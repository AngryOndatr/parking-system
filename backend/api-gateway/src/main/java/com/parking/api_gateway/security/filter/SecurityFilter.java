package com.parking.api_gateway.security.filter;

import com.parking.api_gateway.security.service.JwtTokenService;
import com.parking.api_gateway.security.service.SecurityAuditService;
import com.parking.api_gateway.observability.service.ObservabilityService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute early in filter chain
public class SecurityFilter extends OncePerRequestFilter {
    
    private final JwtTokenService jwtTokenService;
    private final SecurityAuditService auditService;
    private final ObservabilityService observabilityService;
    
    // Rate limiting configuration
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int BRUTE_FORCE_THRESHOLD = 10; // per IP per hour
    
    // Rate limiting storage
    private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> suspiciousIps = new ConcurrentHashMap<>();
    
    // Public endpoints that don't require authentication
    private final List<String> publicPaths = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/health",
            "/actuator/health",
            "/api/docs",
            "/api/swagger-ui",
            "/api/v3/api-docs",
            "/api/clients"  // TODO: TEMPORARY - for testing proxy functionality
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        String method = request.getMethod();
        
        log.debug("Security filter processing request: {} {} from IP: {}", method, path, clientIp);
        
        try {
            // 1. Rate limiting check
            if (!checkRateLimit(clientIp, path)) {
                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
                return;
            }
            
            // 2. Brute force protection
            if (isSuspiciousIp(clientIp)) {
                auditService.logSuspiciousActivity("unknown", clientIp, 
                        "Blocked suspicious IP", "Continued requests from blocked IP");
                sendErrorResponse(response, HttpStatus.FORBIDDEN, 
                        "IP temporarily blocked due to suspicious activity");
                return;
            }
            
            // 3. Check if authentication required
            if (isPublicPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // 4. JWT validation for protected endpoints
            if (validateJwtToken(request, clientIp)) {
                filterChain.doFilter(request, response);
            } else {
                incrementFailedAttempts(clientIp);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication required");
            }
            
        } catch (Exception e) {
            log.error("Security filter error: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Security validation failed");
        }
    }
    
    private boolean checkRateLimit(String clientIp, String path) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitInfo info = rateLimitCache.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        
        // Clean old entries
        info.requestTimestamps.removeIf(timestamp -> 
                ChronoUnit.MINUTES.between(timestamp, now) > 60);
        
        // Check minute limit
        long recentRequests = info.requestTimestamps.stream()
                .filter(timestamp -> ChronoUnit.MINUTES.between(timestamp, now) < 1)
                .count();
        
        if (recentRequests >= MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for IP: {} - {} requests in last minute", clientIp, recentRequests);
            auditService.logSuspiciousActivity("unknown", clientIp, "Rate limit exceeded", 
                    "Too many requests per minute");
            return false;
        }
        
        // Check hour limit
        if (info.requestTimestamps.size() >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Hourly rate limit exceeded for IP: {} - {} requests", clientIp, info.requestTimestamps.size());
            return false;
        }
        
        info.requestTimestamps.add(now);
        return true;
    }
    
    private boolean isSuspiciousIp(String clientIp) {
        LocalDateTime suspicionTime = suspiciousIps.get(clientIp);
        if (suspicionTime == null) {
            return false;
        }
        
        // Block for 1 hour
        if (ChronoUnit.HOURS.between(suspicionTime, LocalDateTime.now()) < 1) {
            return true;
        } else {
            suspiciousIps.remove(clientIp);
            return false;
        }
    }
    
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(publicPath -> 
                path.startsWith(publicPath) || path.equals(publicPath));
    }
    
    private boolean validateJwtToken(HttpServletRequest request, String clientIp) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Missing or invalid Authorization header from IP: {}", clientIp);
            return false;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Use the validateAccessToken method which returns Mono<Claims>
            Claims claims = jwtTokenService.validateAccessToken(token, clientIp).block();
            
            if (claims == null) {
                log.debug("Invalid JWT token from IP: {}", clientIp);
                return false;
            }
            
            String username = claims.getSubject();
            
            // Add user info to request attributes for downstream services
            request.setAttribute("username", username);
            request.setAttribute("user_id", claims.get("user_id"));
            request.setAttribute("roles", claims.get("roles"));
            
            log.debug("JWT token validated successfully for user: {} from IP: {}", username, clientIp);
            return true;
            
        } catch (Exception e) {
            log.warn("JWT token validation failed for IP: {} - {}", clientIp, e.getMessage());
            return false;
        }
    }
    
    private void incrementFailedAttempts(String clientIp) {
        RateLimitInfo info = rateLimitCache.computeIfAbsent(clientIp, k -> new RateLimitInfo());
        int failures = info.failedAttempts.incrementAndGet();
        
        if (failures >= BRUTE_FORCE_THRESHOLD) {
            suspiciousIps.put(clientIp, LocalDateTime.now());
            log.warn("IP {} marked as suspicious after {} failed attempts", clientIp, failures);
            auditService.logSuspiciousActivity("unknown", clientIp, "Brute force detected", 
                    "Multiple failed authentication attempts");
        }
    }
    
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
    
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) 
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}", 
                status.getReasonPhrase(), message, LocalDateTime.now()));
    }
    
    // Inner class for rate limiting info
    private static class RateLimitInfo {
        private final AtomicInteger failedAttempts = new AtomicInteger(0);
        private final List<LocalDateTime> requestTimestamps = new java.util.concurrent.CopyOnWriteArrayList<>();
    }
}