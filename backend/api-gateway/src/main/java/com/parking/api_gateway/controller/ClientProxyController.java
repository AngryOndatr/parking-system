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
 * Proxy controller for Client Service
 * Routes requests from API Gateway to Client Service
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientProxyController {

    private final RestTemplate restTemplate;
    private static final String CLIENT_SERVICE_URL = "http://client-service:8081";

    /**
     * Proxy GET request to fetch all clients
     */
    @GetMapping
    public ResponseEntity<?> getAllClients(HttpServletRequest request) {
        log.info("🚀 [PROXY CONTROLLER START] Received GET /api/clients request");
        log.info("📍 [PROXY CONTROLLER] Remote IP: {}", request.getRemoteAddr());
        log.info("📍 [PROXY CONTROLLER] Request URI: {}", request.getRequestURI());
        log.info("📍 [PROXY CONTROLLER] Username from request: {}", request.getAttribute("username"));
        log.info("📍 [PROXY CONTROLLER] Roles from request: {}", request.getAttribute("roles"));

        try {
            HttpHeaders headers = extractHeaders(request);
            log.info("📦 [PROXY CONTROLLER] Headers extracted: {}", headers.keySet());
            log.info("📦 [PROXY CONTROLLER] Authorization header present: {}", headers.containsKey("Authorization"));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String targetUrl = CLIENT_SERVICE_URL + "/api/clients";
            log.info("🎯 [PROXY CONTROLLER] Proxying GET request to: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("✅ [PROXY CONTROLLER] Client Service responded with status: {}", response.getStatusCode());
            log.info("✅ [PROXY CONTROLLER] Response body length: {} bytes",
                response.getBody() != null ? response.getBody().length() : 0);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [PROXY CONTROLLER] Client Service returned error status: {}", e.getStatusCode());
            log.error("❌ [PROXY CONTROLLER] Error message: {}", e.getMessage());
            log.error("❌ [PROXY CONTROLLER] Response body: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [PROXY CONTROLLER] Exception occurred while proxying request", e);
            log.error("💥 [PROXY CONTROLLER] Exception type: {}", e.getClass().getName());
            log.error("💥 [PROXY CONTROLLER] Exception message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Proxy GET request to fetch client by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying GET request to Client Service: /api/clients/{}", id);

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/clients/" + id,
                HttpMethod.GET,
                entity,
                String.class
            );

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Proxy POST request to create a new client
     */
    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody String clientData, HttpServletRequest request) {
        log.info("Proxying POST request to Client Service: /api/clients");

        try {
            HttpHeaders headers = extractHeaders(request);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(clientData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/clients",
                HttpMethod.POST,
                entity,
                String.class
            );

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Extract headers from incoming request
     */
    private HttpHeaders extractHeaders(HttpServletRequest request) {
        log.info("🔧 [EXTRACT HEADERS] Starting header extraction");
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Forward important headers, skip host-specific ones
                if (!headerName.equalsIgnoreCase("host") &&
                    !headerName.equalsIgnoreCase("connection")) {

                    String headerValue = request.getHeader(headerName);
                    headers.put(headerName, Collections.list(request.getHeaders(headerName)));

                    // Mask sensitive data in logs
                    if (headerName.equalsIgnoreCase("Authorization")) {
                        log.info("🔧 [EXTRACT HEADERS] {}: Bearer ***", headerName);
                    } else {
                        log.info("🔧 [EXTRACT HEADERS] {}: {}", headerName, headerValue);
                    }
                }
            }
        }

        log.info("🔧 [EXTRACT HEADERS] Total headers extracted: {}", headers.size());
        return headers;
    }
}

