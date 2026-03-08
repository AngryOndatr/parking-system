package com.parking.api_gateway.security.filter;

import com.parking.api_gateway.security.service.JwtTokenService;
import com.parking.api_gateway.security.service.SecurityAuditService;
import com.parking.api_gateway.security.entity.UserSecurityEntity.Role;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    // Whitelisted IPs (internal Docker network and monitoring services)
    private final List<String> whitelistedIps = Arrays.asList(
            "127.0.0.1",
            "::1",
            "172.19.0.5",  // Prometheus
            "172.19.0.6",  // Grafana
            "172.19.0.7",  // Other monitoring
            "172.20.0.5",  // Alternative Docker network
            "172.20.0.6",
            "172.20.0.7"
    );

    // Public endpoints that don't require authentication
    private final List<String> publicPaths = Arrays.asList(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/health",
            "/actuator/health",
            "/actuator/prometheus",
            "/actuator/metrics",
            "/actuator/info",
            "/api/docs",
            "/api/swagger-ui",
            "/api/v3/api-docs",
            // Management Service - Public endpoints for clients and info boards
            "/api/management/spots/available",              // List available spots
            "/api/management/spots/available/count",        // Count available spots
            "/api/management/spots/available/lot/",         // Available spots by lot (prefix)
            "/api/management/spots/search"                  // Search spots with filters
            // Reporting Service endpoints require JWT authentication
    );

    // RBAC: route prefix → allowed roles (checked after JWT validation)
    // Key format: "METHOD:/path/prefix"
    // Rules applied in order; first match wins.
    private static final Map<String, Set<Role>> ROUTE_ROLES;
    static {
        Map<String, Set<Role>> m = new HashMap<>();
        // Gate Control Service — write operations
        m.put("POST:/api/v1/gate/",    EnumSet.of(Role.OPERATOR, Role.ADMIN));
        m.put("PUT:/api/v1/gate/",     EnumSet.of(Role.OPERATOR, Role.ADMIN));
        m.put("DELETE:/api/v1/gate/",  EnumSet.of(Role.OPERATOR, Role.ADMIN));
        // Billing Service — write operations
        m.put("POST:/api/v1/billing/",    EnumSet.of(Role.OPERATOR, Role.ADMIN));
        m.put("PUT:/api/v1/billing/",     EnumSet.of(Role.OPERATOR, Role.ADMIN));
        m.put("DELETE:/api/v1/billing/",  EnumSet.of(Role.OPERATOR, Role.ADMIN));
        // Client Service — all methods
        m.put("GET:/api/clients/",    EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        m.put("POST:/api/clients/",   EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        m.put("PUT:/api/clients/",    EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        m.put("DELETE:/api/clients/", EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        // Management Service — write operations
        m.put("POST:/api/management/",   EnumSet.of(Role.ADMIN, Role.MANAGER));
        m.put("PUT:/api/management/",    EnumSet.of(Role.ADMIN, Role.MANAGER));
        m.put("DELETE:/api/management/", EnumSet.of(Role.ADMIN, Role.MANAGER));
        // Reporting Service — read only
        m.put("GET:/api/reporting/",    EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        m.put("POST:/api/reporting/",   EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        m.put("PUT:/api/reporting/",    EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        m.put("DELETE:/api/reporting/", EnumSet.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR));
        ROUTE_ROLES = Map.copyOf(m);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        log.info("🔍 [SECURITY FILTER START] Processing request: {} {} from IP: {}", method, path, clientIp);
        log.info("🔑 [SECURITY FILTER] Authorization header present: {}", authHeader != null ? "Yes (length: " + authHeader.length() + ")" : "No");
        log.info("📋 [SECURITY FILTER] Request headers: {}", getAllHeaders(request));

        try {
            // 0. Check whitelist for internal services
            if (isWhitelistedIp(clientIp)) {
                log.info("✅ [STEP 0] IP {} is whitelisted (internal service/monitoring) - bypassing security checks", clientIp);
                filterChain.doFilter(request, response);
                return;
            }

            // Also check if it's internal Docker network
            if (isInternalDockerIp(clientIp)) {
                log.info("✅ [STEP 0] IP {} is from internal Docker network - bypassing security checks", clientIp);
                filterChain.doFilter(request, response);
                return;
            }

            // 0.5 CORS preflight: OPTIONS requests are fully handled by CorsFilter (@Order 0).
            // SecurityFilter must not block them (no JWT is present in a preflight).
            if ("OPTIONS".equalsIgnoreCase(method)) {
                log.debug("✅ [STEP 0.5] OPTIONS preflight — already handled by CorsFilter, passing through");
                filterChain.doFilter(request, response);
                return;
            }

            // 1. Rate limiting check
            log.info("✓ [STEP 1/4] Checking rate limit for IP: {}", clientIp);
            if (!checkRateLimit(clientIp, path)) {
                log.error("❌ [SECURITY FILTER] BLOCKED at STEP 1 - Rate limit exceeded for IP: {}", clientIp);
                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
                return;
            }
            log.info("✅ [STEP 1/4] Rate limit check passed");

            // 2. Brute force protection
            log.info("✓ [STEP 2/4] Checking if IP is suspicious: {}", clientIp);
            if (isSuspiciousIp(clientIp)) {
                log.error("❌ [SECURITY FILTER] BLOCKED at STEP 2 - Suspicious IP detected: {}", clientIp);
                auditService.logSuspiciousActivity("unknown", clientIp,
                        "Blocked suspicious IP", "Continued requests from blocked IP");
                sendErrorResponse(response, HttpStatus.FORBIDDEN, 
                        "IP temporarily blocked due to suspicious activity");
                return;
            }
            log.info("✅ [STEP 2/4] Brute force protection check passed");

            // 3. Check if authentication required
            log.info("✓ [STEP 3/4] Checking if path is public: {}", path);
            if (isPublicPath(path)) {
                log.info("✅ [STEP 3/4] Path {} is public, skipping authentication", path);
                log.info("🚀 [SECURITY FILTER] Passing request to next filter in chain");
                filterChain.doFilter(request, response);
                log.info("✅ [SECURITY FILTER END] Request completed with status: {}", response.getStatus());
                return;
            }
            
            log.info("🔒 [STEP 3/4] Path {} requires authentication - proceeding to JWT validation", path);

            // 4. JWT validation for protected endpoints
            log.info("✓ [STEP 4/4] Validating JWT token");
            if (validateJwtToken(request, clientIp)) {
                log.info("✅ [STEP 4/4] JWT token validated successfully");
                log.info("👤 [SECURITY FILTER] User authenticated: {}", request.getAttribute("username"));

                // 4.5 RBAC: check that the authenticated user's role is allowed for this route
                String userRole = (String) request.getAttribute("role");
                if (!isRoleAllowed(method, path, userRole)) {
                    log.warn("🚫 [STEP 4.5] RBAC denied: role='{}' method='{}' path='{}'", userRole, method, path);
                    sendErrorResponse(response, HttpStatus.FORBIDDEN,
                            "Access denied: insufficient role for this operation");
                    return;
                }
                log.info("✅ [STEP 4.5] RBAC check passed for role='{}' on {} {}", userRole, method, path);

                log.info("🚀 [SECURITY FILTER] Passing request to next filter in chain");
                filterChain.doFilter(request, response);
                log.info("✅ [SECURITY FILTER END] Request completed with status: {}", response.getStatus());
            } else {
                log.error("❌ [SECURITY FILTER] BLOCKED at STEP 4 - JWT token validation failed");
                log.error("❌ [SECURITY FILTER] Returning 401 UNAUTHORIZED");
                incrementFailedAttempts(clientIp);
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Authentication required");
            }
            
        } catch (Exception e) {
            log.error("💥 [SECURITY FILTER] EXCEPTION occurred: {}", e.getMessage(), e);
            log.error("❌ [SECURITY FILTER] Returning 500 INTERNAL_SERVER_ERROR");
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Security validation failed");
        }
    }
    
    private String getAllHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // Mask sensitive headers
            if (headerName.equalsIgnoreCase("Authorization")) {
                headerValue = headerValue != null ? "Bearer ***" : null;
            }
            headers.append(headerName).append("=").append(headerValue).append(", ");
        }
        return headers.toString();
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
    
    private boolean isWhitelistedIp(String clientIp) {
        boolean isWhitelisted = whitelistedIps.contains(clientIp);
        if (isWhitelisted) {
            log.info("🏷️ [WHITELIST] IP {} found in whitelist", clientIp);
        }
        return isWhitelisted;
    }

    private boolean isInternalDockerIp(String clientIp) {
        // Check for common Docker internal networks
        boolean isInternal = clientIp.startsWith("172.") ||
                             clientIp.startsWith("10.") ||
                             clientIp.startsWith("192.168.") ||
                             clientIp.equals("127.0.0.1") ||
                             clientIp.equals("::1") ||
                             clientIp.equals("0:0:0:0:0:0:0:1");

        if (isInternal) {
            log.info("🐳 [DOCKER NETWORK] IP {} detected as internal Docker network address", clientIp);
        }
        return isInternal;
    }

    private boolean validateJwtToken(HttpServletRequest request, String clientIp) {
        String authHeader = request.getHeader("Authorization");
        
        log.info("🔐 [JWT VALIDATION] Starting JWT validation for IP: {}", clientIp);
        log.info("🔐 [JWT VALIDATION] Authorization header: {}", authHeader != null ? "Present" : "Missing");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🔐 [JWT VALIDATION] FAILED - Missing or invalid Authorization header from IP: {}", clientIp);
            log.warn("🔐 [JWT VALIDATION] Expected format: 'Bearer <token>'");
            return false;
        }
        
        String token = authHeader.substring(7);
        log.info("🔐 [JWT VALIDATION] Token extracted, length: {} characters", token.length());
        log.debug("🔐 [JWT VALIDATION] Token (first 20 chars): {}...", token.substring(0, Math.min(20, token.length())));

        try {
            log.info("🔐 [JWT VALIDATION] Calling jwtTokenService.validateAccessToken()...");
            Claims claims = jwtTokenService.validateAccessToken(token, clientIp).block();
            
            if (claims == null) {
                log.error("🔐 [JWT VALIDATION] FAILED - validateAccessToken returned null claims");
                return false;
            }
            
            String username = claims.getSubject();
            log.info("🔐 [JWT VALIDATION] Claims retrieved successfully");
            log.info("🔐 [JWT VALIDATION] Username from token: {}", username);
            log.info("🔐 [JWT VALIDATION] User ID: {}", claims.get("userId"));
            log.info("🔐 [JWT VALIDATION] Role: {}", claims.get("role"));

            // Add user info to request attributes for downstream services
            request.setAttribute("username", username);
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("role", claims.get("role"));

            log.info("🔐 [JWT VALIDATION] SUCCESS - User '{}' authenticated from IP: {}", username, clientIp);
            return true;
            
        } catch (Exception e) {
            log.error("🔐 [JWT VALIDATION] FAILED - Exception during validation: {}", e.getMessage());
            log.error("🔐 [JWT VALIDATION] Exception type: {}", e.getClass().getName());
            log.error("🔐 [JWT VALIDATION] Stack trace:", e);
            return false;
        }
    }
    
    /**
     * RBAC check: returns true if no rule exists for this route (open) or
     * if the user's role is in the allowed set for the matched rule.
     *
     * @param method   HTTP method (GET, POST, …)
     * @param path     request URI
     * @param roleStr  role string extracted from JWT claim "role" (may be null)
     * @return true → allow, false → deny with 403
     */
    boolean isRoleAllowed(String method, String path, String roleStr) {
        for (Map.Entry<String, Set<Role>> entry : ROUTE_ROLES.entrySet()) {
            String key = entry.getKey();
            int sep = key.indexOf(':');
            String ruleMethod = key.substring(0, sep);
            String rulePrefix = key.substring(sep + 1);

            if (ruleMethod.equalsIgnoreCase(method) && path.startsWith(rulePrefix)) {
                // Rule matched — check role
                if (roleStr == null) {
                    return false;
                }
                try {
                    Role userRole = Role.valueOf(roleStr);
                    return entry.getValue().contains(userRole);
                } catch (IllegalArgumentException e) {
                    log.warn("🚫 [RBAC] Unknown role in JWT: '{}'", roleStr);
                    return false;
                }
            }
        }
        // No rule matched → no restriction → allow
        return true;
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