package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Proxy controller for subscription-related endpoints from client-service.
 *
 * These endpoints live at /api/v1/clients/... (OpenAPI-first path in client-service)
 * which is separate from the legacy /api/clients/... proxy handled by ClientProxyController.
 *
 * Used by:
 *  - full-rebuild.ps1 smoke tests (test 17)
 *  - Future frontend calls to check subscriber status
 *
 * Gate-control-service calls client-service directly (not through api-gateway),
 * so no proxy is needed for internal service communication.
 */
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionProxyController {

    private final RestTemplate restTemplate;
    private static final String CLIENT_SERVICE_URL = "http://client-service:8081";

    /**
     * Proxy GET /api/v1/clients/subscriptions/check?licensePlate=...
     * Forwards to client-service which does the real DB lookup.
     */
    @GetMapping("/subscriptions/check")
    public ResponseEntity<?> checkSubscription(
            @RequestParam String licensePlate,
            HttpServletRequest request) {

        log.info("🔍 [SUBSCRIPTION PROXY] GET /api/v1/clients/subscriptions/check?licensePlate={}", licensePlate);

        try {
            HttpHeaders headers = new HttpHeaders();
            // Forward Authorization header so client-service JWT filter can validate it
            String auth = request.getHeader("Authorization");
            if (auth != null) {
                headers.set("Authorization", auth);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String targetUrl = CLIENT_SERVICE_URL
                    + "/api/v1/clients/subscriptions/check?licensePlate="
                    + java.net.URLEncoder.encode(licensePlate, java.nio.charset.StandardCharsets.UTF_8);

            log.info("🎯 [SUBSCRIPTION PROXY] Forwarding to: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl, HttpMethod.GET, entity, String.class);

            log.info("✅ [SUBSCRIPTION PROXY] client-service responded: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [SUBSCRIPTION PROXY] client-service error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [SUBSCRIPTION PROXY] Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }
}

