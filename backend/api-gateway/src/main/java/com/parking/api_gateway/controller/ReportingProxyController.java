package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Proxy controller for Reporting Service
 * Routes requests from API Gateway to Reporting Service
 */
@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
@Slf4j
public class ReportingProxyController {

    private final RestTemplate restTemplate;
    private static final String REPORTING_SERVICE_URL = "http://reporting-service:8080";

    /**
     * Proxy POST request to create a log entry
     */
    @PostMapping("/log")
    public ResponseEntity<?> createLog(@RequestBody String body, HttpServletRequest request) {
        log.info("🚀 [REPORTING PROXY] POST /api/reporting/log");
        log.info("📦 [REPORTING PROXY] Request body length: {} bytes", body != null ? body.length() : 0);

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String targetUrl = REPORTING_SERVICE_URL + "/api/reporting/log";
            log.info("🎯 [REPORTING PROXY] Proxying to: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.info("✅ [REPORTING PROXY] Reporting Service responded: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [REPORTING PROXY] Reporting Service error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [REPORTING PROXY] Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Reporting Service: " + e.getMessage());
        }
    }

    /**
     * Proxy GET request to fetch all log entries with optional filters
     */
    @GetMapping("/logs")
    public ResponseEntity<?> getAllLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {

        log.info("🚀 [REPORTING PROXY] GET /api/reporting/logs with filters: level={}, service={}, userId={}, limit={}",
                level, service, userId, limit);

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Build URL with query parameters
            StringBuilder urlBuilder = new StringBuilder(REPORTING_SERVICE_URL + "/api/reporting/logs");
            boolean firstParam = true;

            if (level != null) {
                urlBuilder.append(firstParam ? "?" : "&").append("level=").append(level);
                firstParam = false;
            }
            if (service != null) {
                urlBuilder.append(firstParam ? "?" : "&").append("service=").append(service);
                firstParam = false;
            }
            if (userId != null) {
                urlBuilder.append(firstParam ? "?" : "&").append("userId=").append(userId);
                firstParam = false;
            }
            if (fromDate != null) {
                urlBuilder.append(firstParam ? "?" : "&").append("fromDate=").append(fromDate);
                firstParam = false;
            }
            if (toDate != null) {
                urlBuilder.append(firstParam ? "?" : "&").append("toDate=").append(toDate);
                firstParam = false;
            }
            if (limit != null) {
                urlBuilder.append(firstParam ? "?" : "&").append("limit=").append(limit);
                firstParam = false;
            }

            String targetUrl = urlBuilder.toString();
            log.info("🎯 [REPORTING PROXY] Proxying to: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("✅ [REPORTING PROXY] Reporting Service responded: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [REPORTING PROXY] Reporting Service error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [REPORTING PROXY] Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Reporting Service: " + e.getMessage());
        }
    }

    /**
     * Proxy GET /api/reporting/audit — all business-event audit logs
     */
    @GetMapping("/audit")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {
        return proxyGet(buildUrl("/api/reporting/audit",
                param("service", service), param("from", from), param("to", to), param("limit", limit)),
                request);
    }

    /**
     * Proxy GET /api/reporting/audit/client/{clientId} — client history
     */
    @GetMapping("/audit/client/{clientId}")
    public ResponseEntity<?> getClientHistory(
            @PathVariable Long clientId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {
        return proxyGet(buildUrl("/api/reporting/audit/client/" + clientId,
                param("from", from), param("to", to), param("limit", limit)),
                request);
    }

    /**
     * Proxy GET /api/reporting/audit/vehicle/{licensePlate} — vehicle history
     */
    @GetMapping("/audit/vehicle/{licensePlate}")
    public ResponseEntity<?> getVehicleHistory(
            @PathVariable String licensePlate,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            HttpServletRequest request) {
        return proxyGet(buildUrl("/api/reporting/audit/vehicle/" + licensePlate,
                param("from", from), param("to", to), param("limit", limit)),
                request);
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private ResponseEntity<?> proxyGet(String targetUrl, HttpServletRequest request) {
        log.info("🎯 [REPORTING PROXY] GET {}", targetUrl);
        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(targetUrl, HttpMethod.GET, entity, String.class);
            log.info("✅ [REPORTING PROXY] responded: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [REPORTING PROXY] {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Reporting Service: " + e.getMessage());
        }
    }

    private String buildUrl(String path, String... queryParts) {
        StringBuilder sb = new StringBuilder(REPORTING_SERVICE_URL + path);
        boolean first = true;
        for (String part : queryParts) {
            if (part != null) {
                sb.append(first ? "?" : "&").append(part);
                first = false;
            }
        }
        return sb.toString();
    }

    private String param(String name, Object value) {
        return value != null ? name + "=" + value : null;
    }

    /**
     * Extract headers from incoming request.
     * Hop-by-hop and Accept-Encoding headers are stripped via ProxyUtils so that
     * RestTemplate can transparently decompress gzip and we avoid double-chunked encoding.
     */
    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (ProxyUtils.shouldForwardRequestHeader(headerName)) {
                    headers.put(headerName, Collections.list(request.getHeaders(headerName)));
                }
            }
        }

        // Ensure Content-Type is set
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        // Add username and roles from security context if available
        Object username = request.getAttribute("username");
        Object roles = request.getAttribute("roles");

        if (username != null) {
            headers.add("X-Username", username.toString());
        }
        if (roles != null) {
            headers.add("X-Roles", roles.toString());
        }

        log.debug("📋 [REPORTING PROXY] Forwarding headers: {}", headers.keySet());

        return headers;
    }
}

