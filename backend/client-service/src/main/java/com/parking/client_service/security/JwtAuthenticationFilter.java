package com.parking.client_service.security;

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

        log.info("🔍 [CLIENT-SERVICE FILTER START] Processing request: {} {} from IP: {}", method, path, clientIp);
        log.info("🔑 [CLIENT-SERVICE FILTER] Authorization header present: {}", authHeader != null ? "Yes" : "No");

        // Skip JWT validation for public endpoints
        if (path.startsWith("/actuator") || path.startsWith("/api/health") ||
            path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            log.info("✅ [CLIENT-SERVICE FILTER] Public endpoint, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔒 [CLIENT-SERVICE FILTER] Protected endpoint, validating JWT");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("🔐 [CLIENT-SERVICE FILTER] Token extracted, length: {} characters", token.length());

            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    String role = jwtTokenProvider.getRoleFromToken(token);
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);

                    log.info("✅ [CLIENT-SERVICE FILTER] Token validated successfully");
                    log.info("👤 [CLIENT-SERVICE FILTER] Username: {}, Role: {}, UserId: {}", username, role, userId);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ [CLIENT-SERVICE FILTER] Authentication set in SecurityContext");
                } else {
                    log.warn("❌ [CLIENT-SERVICE FILTER] Token validation failed - invalid token");
                }
            } catch (Exception e) {
                log.error("❌ [CLIENT-SERVICE FILTER] Exception during JWT validation: {}", e.getMessage(), e);
            }
        } else {
            log.warn("❌ [CLIENT-SERVICE FILTER] Missing or invalid Authorization header");
        }

        log.info("🚀 [CLIENT-SERVICE FILTER] Passing request to next filter");
        filterChain.doFilter(request, response);
        log.info("✅ [CLIENT-SERVICE FILTER END] Request completed with status: {}", response.getStatus());
    }
}

