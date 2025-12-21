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
    private static final String CLIENT_SERVICE_URL = "http://client-service:8080";

    /**
     * Proxy GET request to fetch all clients
     */
    @GetMapping
    public ResponseEntity<?> getAllClients(HttpServletRequest request) {
        log.info("Proxying GET request to Client Service: /api/clients");

        try {
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                CLIENT_SERVICE_URL + "/api/clients",
                HttpMethod.GET,
                entity,
                String.class
            );

            log.info("Client Service responded with status: {}", response.getStatusCode());
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
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Forward important headers, skip host-specific ones
                if (!headerName.equalsIgnoreCase("host") &&
                    !headerName.equalsIgnoreCase("connection")) {
                    headers.put(headerName, Collections.list(request.getHeaders(headerName)));
                }
            }
        }

        return headers;
    }
}

