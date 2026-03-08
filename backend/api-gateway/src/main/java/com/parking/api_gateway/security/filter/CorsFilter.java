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
import java.util.Set;

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

    /** Origins allowed to call the api-gateway. */
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:5173",   // Vite dev server (React default)
            "http://localhost:3000",   // CRA / alternative dev port
            "null"                     // file:// protocol — devops/test-login.html opened locally
    );

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

        // Only add CORS headers when the request carries a recognised Origin
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin",  origin);
            response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
            response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
            response.setHeader("Access-Control-Expose-Headers", EXPOSED_HEADERS);
            response.setHeader("Access-Control-Max-Age",       MAX_AGE);
            // allowCredentials intentionally omitted / false — JWT via Authorization header
            log.debug("✅ [CORS] Headers added for origin: {}", origin);
        }

        // Handle OPTIONS preflight: respond 200 immediately, skip SecurityFilter
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("✅ [CORS] OPTIONS preflight for {} — returning 200, bypassing SecurityFilter", request.getRequestURI());
            response.setStatus(HttpStatus.OK.value());
            return; // do NOT call filterChain.doFilter()
        }

        filterChain.doFilter(request, response);
    }
}

