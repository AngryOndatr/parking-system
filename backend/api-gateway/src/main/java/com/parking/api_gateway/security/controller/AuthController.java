package com.parking.api_gateway.security.controller;

import com.parking.api_gateway.security.dto.AuthRequest;
import com.parking.api_gateway.security.dto.AuthResponse;
import com.parking.api_gateway.security.service.UserSecurityService;
import com.parking.api_gateway.security.service.JwtTokenService;
import com.parking.api_gateway.security.service.SecurityAuditService;
import com.parking.api_gateway.security.entity.UserSecurityEntity;
import com.parking.api_gateway.security.repository.UserSecurityRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {
    
    private final UserSecurityService userSecurityService;
    private final JwtTokenService jwtTokenService;
    private final SecurityAuditService auditService;
    private final UserSecurityRepository userRepository;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked"),
        @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest request,
                                                   HttpServletRequest httpRequest) {
        
        String clientIpAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        log.info("Login attempt for user: {} from IP: {}", request.getUsername(), clientIpAddress);
        
        return userSecurityService.authenticateUser(request, clientIpAddress, userAgent)
                .map(authResponse -> {
                    // Create JWT tokens
                    String accessToken = jwtTokenService.createAccessToken(
                            authResponse.getUser(), clientIpAddress, userAgent);
                    String refreshToken = jwtTokenService.createRefreshToken(
                            authResponse.getUser(), clientIpAddress);
                    
                    // Complete auth response
                    authResponse.setAccessToken(accessToken);
                    authResponse.setRefreshToken(refreshToken);
                    authResponse.setSessionTimeoutMinutes(480); // 8 hours
                    
                    log.info("Login successful for user: {} from IP: {}", 
                            authResponse.getUser().getUsername(), clientIpAddress);
                    
                    return ResponseEntity.ok(authResponse);
                })
                .onErrorResume(throwable -> {
                    log.error("Login failed for user: {} from IP: {} - Error: {}", 
                             request.getUsername(), clientIpAddress, throwable.getMessage());
                    
                    return Mono.just(createErrorResponse(throwable, clientIpAddress));
                });
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public Mono<ResponseEntity<?>> refreshToken(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String refreshToken = request.get("refreshToken");
        String clientIpAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Refresh token is required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMap));
        }
        
        return jwtTokenService.validateRefreshToken(refreshToken)
                .flatMap(userId -> {
                    Optional<UserSecurityEntity> userOpt = userRepository.findById(userId);
                    if (userOpt.isEmpty()) {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("error", "User not found");
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(errorMap));
                    }
                    
                    UserSecurityEntity user = userOpt.get();
                    
                    // Create new access token
                    String newAccessToken = jwtTokenService.createAccessToken(
                            user, clientIpAddress, userAgent);
                    
                    auditService.logTokenCreated(user.getUsername(), "ACCESS_REFRESH", clientIpAddress);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("accessToken", newAccessToken);
                    response.put("expiresIn", 3600);
                    response.put("tokenType", "Bearer");
                    
                    return Mono.just((ResponseEntity<?>) ResponseEntity.ok(response));
                })
                .onErrorResume(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid refresh token");
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse));
                });
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidate user tokens and logout")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    public Mono<ResponseEntity<?>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String clientIpAddress = getClientIpAddress(httpRequest);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Authorization header required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMap));
        }
        
        String accessToken = authHeader.substring(7);
        String refreshToken = request != null ? request.get("refreshToken") : null;
        
        return jwtTokenService.blacklistToken(accessToken, "User logout")
                .then(refreshToken != null ? 
                        jwtTokenService.blacklistToken(refreshToken, "User logout") : 
                        Mono.empty())
                .then(Mono.<ResponseEntity<?>>fromCallable(() -> {
                    auditService.logUserLogout("user", clientIpAddress, "User initiated logout");
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("message", "Logout successful");
                    return ResponseEntity.ok(responseMap);
                }))
                .onErrorResume(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Logout failed");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse));
                });
    }
    
    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", description = "Invalidate all user sessions")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All sessions invalidated"),
        @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    public Mono<ResponseEntity<?>> logoutAll(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
        
        String clientIpAddress = getClientIpAddress(httpRequest);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Authorization header required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMap));
        }
        
        String accessToken = authHeader.substring(7);
        
        return jwtTokenService.validateAccessToken(accessToken, clientIpAddress)
                .flatMap(claims -> {
                    Long userId = claims.get("userId", Long.class);
                    String username = claims.getSubject();
                    
                    return jwtTokenService.invalidateAllUserSessions(userId, "Logout all devices")
                            .then(Mono.<ResponseEntity<?>>fromCallable(() -> {
                                auditService.logUserLogout(username, clientIpAddress, 
                                        "User initiated logout from all devices");
                                Map<String, Object> response = new HashMap<>();
                                response.put("message", "Logged out from all devices successfully");
                                return ResponseEntity.ok(response);
                            }));
                })
                .onErrorResume(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid token");
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse));
                });
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change user password", description = "Change user password with validation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid token or current password"),
        @ApiResponse(responseCode = "400", description = "Invalid password format")
    })
    public Mono<ResponseEntity<?>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String clientIpAddress = getClientIpAddress(httpRequest);
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Current and new passwords are required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMap));
        }
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Authorization header required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMap));
        }
        
        String accessToken = authHeader.substring(7);
        
        return jwtTokenService.validateAccessToken(accessToken, clientIpAddress)
                .flatMap(claims -> {
                    Long userId = claims.get("userId", Long.class);
                    
                    return userSecurityService.changePassword(userId, currentPassword, newPassword)
                            .then(Mono.<ResponseEntity<?>>fromCallable(() -> {
                                Map<String, Object> responseMap = new HashMap<>();
                                responseMap.put("message", "Password changed successfully");
                                return ResponseEntity.ok(responseMap);
                            }));
                })
                .onErrorResume(throwable -> {
                    log.error("Password change failed: {}", throwable.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", throwable.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(errorResponse));
                });
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user profile information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    public Mono<ResponseEntity<?>> getUserProfile(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
        
        String clientIpAddress = getClientIpAddress(httpRequest);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Authorization header required");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorMap));
        }
        
        String accessToken = authHeader.substring(7);
        
        return jwtTokenService.validateAccessToken(accessToken, clientIpAddress)
                .flatMap(claims -> {
                    Long userId = claims.get("userId", Long.class);
                    
                    Optional<UserSecurityEntity> userOpt = userRepository.findById(userId);
                    if (userOpt.isEmpty()) {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("error", "User not found");
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(errorMap));
                    }
                    
                    UserSecurityEntity user = userOpt.get();
                    
                    Map<String, Object> profileResponse = new HashMap<>();
                    profileResponse.put("id", user.getId());
                    profileResponse.put("username", user.getUsername());
                    profileResponse.put("email", user.getEmail());
                    profileResponse.put("firstName", user.getFirstName());
                    profileResponse.put("lastName", user.getLastName());
                    profileResponse.put("role", user.getRole());
                    profileResponse.put("lastLoginAt", user.getLastLoginAt());
                    profileResponse.put("passwordChangedAt", user.getPasswordChangedAt());
                    profileResponse.put("requiresPasswordChange", user.getForcePasswordChange());
                    
                    return Mono.just((ResponseEntity<?>) ResponseEntity.ok(profileResponse));
                })
                .onErrorResume(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid token");
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(errorResponse));
                });
    }
    
    /**
     * Create error response based on exception type
     */
    private ResponseEntity<AuthResponse> createErrorResponse(Throwable throwable, String clientIp) {
        HttpStatus status;
        String message = throwable.getMessage();
        
        if (throwable instanceof com.parking.api_gateway.security.exception.AccountLockedException) {
            status = HttpStatus.LOCKED;
        } else if (throwable instanceof com.parking.api_gateway.security.exception.CredentialsExpiredException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (throwable instanceof com.parking.api_gateway.security.exception.AccountDisabledException) {
            status = HttpStatus.FORBIDDEN;
        } else {
            status = HttpStatus.UNAUTHORIZED;
            message = "Authentication failed";
        }
        
        return ResponseEntity.status(status).body(
                AuthResponse.builder()
                        .user(null)
                        .loginTime(LocalDateTime.now())
                        .ipAddress(clientIp)
                        .build()
        );
    }
    
    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
