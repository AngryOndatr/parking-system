package com.parking.api_gateway.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CORS filter registered at Order(0) — runs before SecurityFilter (Order 1).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Add CORS response headers for every request from an allowed origin.</li>
 *   <li>Return HTTP 200 immediately for OPTIONS preflight requests, bypassing
 *       SecurityFilter entirely (no JWT required for preflight).</li>
 * </ul>
 *
 * <p>Strategy: {@code allowCredentials=false} — the React frontend stores the
 * JWT in localStorage and sends it via the {@code Authorization} header.
 * No cookies are used, so {@code allowCredentials} is intentionally {@code false}.
 * This also lets us declare exact origins without the
 * {@code allowCredentials + wildcard} restriction.</p>
 *
 * @see <a href="https://fetch.spec.whatwg.org/#cors-protocol">CORS specification</a>
 */
@Component
@Order(0)
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://192.168.*,null}")
    private String corsAllowedOrigins;

    private Set<String> getAllowedOrigins() {
        return Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if the origin matches any allowed origin entry.
     * Entries ending with "*" are treated as prefix wildcards
     * (e.g. "http://192.168.1.*" matches any IP in that subnet on any port).
     */
    private boolean isOriginAllowed(String origin) {
        for (String allowed : getAllowedOrigins()) {
            if (allowed.endsWith("*")) {
                String prefix = allowed.substring(0, allowed.length() - 1);
                if (origin.startsWith(prefix)) {
                    return true;
                }
            } else if (allowed.equals(origin)) {
                return true;
            }
        }
        return false;
    }

    private static final String ALLOWED_METHODS  = "GET, POST, PUT, DELETE, PATCH, OPTIONS";
    private static final String ALLOWED_HEADERS  = "Authorization, Content-Type, X-Requested-With";
    private static final String EXPOSED_HEADERS  = "Authorization";
    private static final String MAX_AGE          = "3600";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        String method = request.getMethod();

        if (origin != null && isOriginAllowed(origin)) {
            response.setHeader("Access-Control-Allow-Origin",  origin);
            response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
            response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
            response.setHeader("Access-Control-Expose-Headers", EXPOSED_HEADERS);
            response.setHeader("Access-Control-Max-Age",       MAX_AGE);
            log.debug("✅ [CORS] Headers added for origin: {}", origin);
        }

        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("✅ [CORS] OPTIONS preflight for {} — returning 200, bypassing SecurityFilter", request.getRequestURI());
            response.setStatus(HttpStatus.OK.value());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
