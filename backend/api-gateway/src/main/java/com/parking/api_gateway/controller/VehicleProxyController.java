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
 * Proxy controller for Vehicle operations in Client Service
 * Routes Vehicle-related requests from API Gateway to Client Service
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleProxyController {

    private final RestTemplate restTemplate;
    private static final String CLIENT_SERVICE_URL = "http://client-service:8081";

    /**
     * Proxy GET request to fetch all vehicles
     */
    @GetMapping
    public ResponseEntity<?> getAllVehicles(HttpServletRequest request) {
        log.info("🚀 [VEHICLE PROXY] Proxying GET request to Client Service: /api/vehicles");

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String targetUrl = CLIENT_SERVICE_URL + "/api/vehicles";
            log.info("🎯 [VEHICLE PROXY] Target URL: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("✅ [VEHICLE PROXY] Client Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [VEHICLE PROXY] Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [VEHICLE PROXY] Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Proxy GET request to fetch vehicle by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id, HttpServletRequest request) {
        log.info("🚀 [VEHICLE PROXY] Proxying GET request to Client Service: /api/vehicles/{}", id);

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String targetUrl = CLIENT_SERVICE_URL + "/api/vehicles/" + id;
            log.info("🎯 [VEHICLE PROXY] Target URL: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("✅ [VEHICLE PROXY] Client Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [VEHICLE PROXY] Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [VEHICLE PROXY] Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Proxy POST request to create a new vehicle
     */
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody String vehicleData, HttpServletRequest request) {
        log.info("🚀 [VEHICLE PROXY] Proxying POST request to Client Service: /api/vehicles");

        try {
            HttpHeaders headers = extractHeaders(request);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(vehicleData, headers);

            String targetUrl = CLIENT_SERVICE_URL + "/api/vehicles";
            log.info("🎯 [VEHICLE PROXY] Target URL: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.info("✅ [VEHICLE PROXY] Client Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [VEHICLE PROXY] Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [VEHICLE PROXY] Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Proxy PUT request to update a vehicle
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody String vehicleData, HttpServletRequest request) {
        log.info("🚀 [VEHICLE PROXY] Proxying PUT request to Client Service: /api/vehicles/{}", id);

        try {
            HttpHeaders headers = extractHeaders(request);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(vehicleData, headers);

            String targetUrl = CLIENT_SERVICE_URL + "/api/vehicles/" + id;
            log.info("🎯 [VEHICLE PROXY] Target URL: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.PUT,
                entity,
                String.class
            );

            log.info("✅ [VEHICLE PROXY] Client Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [VEHICLE PROXY] Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [VEHICLE PROXY] Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Proxy DELETE request to delete a vehicle
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, HttpServletRequest request) {
        log.info("🚀 [VEHICLE PROXY] Proxying DELETE request to Client Service: /api/vehicles/{}", id);

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String targetUrl = CLIENT_SERVICE_URL + "/api/vehicles/" + id;
            log.info("🎯 [VEHICLE PROXY] Target URL: {}", targetUrl);

            ResponseEntity<String> response = restTemplate.exchange(
                targetUrl,
                HttpMethod.DELETE,
                entity,
                String.class
            );

            log.info("✅ [VEHICLE PROXY] Client Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ [VEHICLE PROXY] Client Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("💥 [VEHICLE PROXY] Error proxying request to Client Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }

    /**
     * Extract headers from incoming request
     */
    private HttpHeaders extractHeaders(HttpServletRequest request) {
        log.debug("🔧 [VEHICLE PROXY] Extracting headers");
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Forward important headers, skip host-specific ones
                if (!headerName.equalsIgnoreCase("host") &&
                    !headerName.equalsIgnoreCase("connection")) {

                    headers.put(headerName, Collections.list(request.getHeaders(headerName)));

                    // Mask sensitive data in logs
                    if (headerName.equalsIgnoreCase("Authorization")) {
                        log.debug("🔧 [VEHICLE PROXY] {}: Bearer ***", headerName);
                    } else {
                        log.debug("🔧 [VEHICLE PROXY] {}: {}", headerName, request.getHeader(headerName));
                    }
                }
            }
        }

        log.debug("🔧 [VEHICLE PROXY] Total headers extracted: {}", headers.size());
        return headers;
    }
}

