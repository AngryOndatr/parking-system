package com.parking.api_gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Interceptor for automatically forwarding JWT tokens to downstream services
 */
@Component
@Slf4j
public class JwtRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                       ClientHttpRequestExecution execution) throws IOException {

        // Get current HTTP request context
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest httpRequest = attributes.getRequest();

            // Extract Authorization header from incoming request
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Forward JWT token to downstream service
                request.getHeaders().add("Authorization", authHeader);
                log.debug("Forwarding JWT token to downstream service: {}", request.getURI());
            }

            // Forward other important headers
            String clientIp = getClientIpAddress(httpRequest);
            if (clientIp != null) {
                request.getHeaders().add("X-Forwarded-For", clientIp);
            }

            String userAgent = httpRequest.getHeader("User-Agent");
            if (userAgent != null) {
                request.getHeaders().add("User-Agent", userAgent);
            }
        }

        return execution.execute(request, body);
    }

    /**
     * Extract client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

