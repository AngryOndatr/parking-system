package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Proxy controller for Gate Control Service
 * Routes gate control requests from API Gateway to Gate Control Service
 */
@RestController
@RequestMapping("/api/gate")
@RequiredArgsConstructor
@Slf4j
public class GateControlProxyController {

    private final RestTemplate restTemplate;
    private static final String GATE_SERVICE_URL = "http://gate-control-service:8080";

    /**
     * Proxy POST request to open gate (entry)
     */
    @PostMapping("/entry")
    public ResponseEntity<?> openEntryGate(@RequestBody String entryData, HttpServletRequest request) {
        log.info("Proxying POST request to Gate Control Service: /api/gate/entry");
        return proxyRequest(HttpMethod.POST, "/api/gate/entry", entryData, request);
    }

    /**
     * Proxy POST request to open gate (exit)
     */
    @PostMapping("/exit")
    public ResponseEntity<?> openExitGate(@RequestBody String exitData, HttpServletRequest request) {
        log.info("Proxying POST request to Gate Control Service: /api/gate/exit");
        return proxyRequest(HttpMethod.POST, "/api/gate/exit", exitData, request);
    }

    /**
     * Proxy GET request to fetch all gate events
     */
    @GetMapping("/events")
    public ResponseEntity<?> getAllGateEvents(HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/gate/events");
        return proxyRequest(HttpMethod.GET, "/api/gate/events", null, request);
    }

    /**
     * Proxy GET request to fetch gate event by ID
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<?> getGateEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/gate/events/{}", id);
        return proxyRequest(HttpMethod.GET, "/api/gate/events/" + id, null, request);
    }

    /**
     * Proxy GET request to fetch gate status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getGateStatus(HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/gate/status");
        return proxyRequest(HttpMethod.GET, "/api/gate/status", null, request);
    }

    /**
     * Proxy GET request to fetch client gate history
     */
    @GetMapping("/clients/{clientId}/history")
    public ResponseEntity<?> getClientGateHistory(@PathVariable Long clientId, HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/gate/clients/{}/history", clientId);
        return proxyRequest(HttpMethod.GET, "/api/gate/clients/" + clientId + "/history", null, request);
    }

    /**
     * Generic proxy method for all HTTP methods
     */
    private ResponseEntity<?> proxyRequest(HttpMethod method, String path, String body,
                                          HttpServletRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                GATE_SERVICE_URL + path,
                method,
                entity,
                String.class
            );

            log.info("Gate Control Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Gate Control Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Gate Control Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Gate Control Service: " + e.getMessage());
        }
    }
}

