package com.parking.reporting_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter for Client Service
 * Validates JWT tokens from API Gateway
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        String clientIp = request.getRemoteAddr();

        log.info("🔍 [REPORTING-SERVICE FILTER START] Processing request: {} {} from IP: {}", method, path, clientIp);
        log.info("🔑 [REPORTING-SERVICE FILTER] Authorization header present: {}", authHeader != null ? "Yes" : "No");

        // Skip JWT validation for public endpoints
        if (path.startsWith("/actuator") || path.startsWith("/api/health") ||
            path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            log.info("✅ [REPORTING-SERVICE FILTER] Public endpoint, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔒 [REPORTING-SERVICE FILTER] Protected endpoint, validating JWT");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("🔐 [REPORTING-SERVICE FILTER] Token extracted, length: {} characters", token.length());

            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    String role = jwtTokenProvider.getRoleFromToken(token);
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);

                    log.info("✅ [REPORTING-SERVICE FILTER] Token validated successfully");
                    log.info("👤 [REPORTING-SERVICE FILTER] Username: {}, Role: {}, UserId: {}", username, role, userId);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ [REPORTING-SERVICE FILTER] Authentication set in SecurityContext");
                } else {
                    log.warn("❌ [REPORTING-SERVICE FILTER] Token validation failed - invalid token");
                }
            } catch (Exception e) {
                log.error("❌ [REPORTING-SERVICE FILTER] Exception during JWT validation: {}", e.getMessage(), e);
            }
        } else {
            log.warn("❌ [REPORTING-SERVICE FILTER] Missing or invalid Authorization header");
        }

        log.info("🚀 [REPORTING-SERVICE FILTER] Passing request to next filter");
        filterChain.doFilter(request, response);
        log.info("✅ [REPORTING-SERVICE FILTER END] Request completed with status: {}", response.getStatus());
    }
}

