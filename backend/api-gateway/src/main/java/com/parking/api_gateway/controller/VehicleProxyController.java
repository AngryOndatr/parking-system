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
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
public class VehicleProxyController {
    private final RestTemplate restTemplate;
    private static final String CLIENT_SERVICE_URL = "http://client-service:8081";
    @GetMapping
    public ResponseEntity<?> getAllVehicles(HttpServletRequest request) {
        log.info("[VEHICLE PROXY] GET /api/vehicles");
        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/vehicles", HttpMethod.GET, entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[VEHICLE PROXY] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id, HttpServletRequest request) {
        log.info("[VEHICLE PROXY] GET /api/vehicles/{}", id);
        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/vehicles/" + id, HttpMethod.GET, entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[VEHICLE PROXY] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody String vehicleData, HttpServletRequest request) {
        log.info("[VEHICLE PROXY] POST /api/vehicles");
        try {
            HttpHeaders headers = extractHeaders(request);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(vehicleData, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/vehicles", HttpMethod.POST, entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[VEHICLE PROXY] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody String vehicleData, HttpServletRequest request) {
        log.info("[VEHICLE PROXY] PUT /api/vehicles/{}", id);
        try {
            HttpHeaders headers = extractHeaders(request);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(vehicleData, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/vehicles/" + id, HttpMethod.PUT, entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[VEHICLE PROXY] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id, HttpServletRequest request) {
        log.info("[VEHICLE PROXY] DELETE /api/vehicles/{}", id);
        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/vehicles/" + id, HttpMethod.DELETE, entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("[VEHICLE PROXY] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Client Service: " + e.getMessage());
        }
    }
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
        return headers;
    }
}