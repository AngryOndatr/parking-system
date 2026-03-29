package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Proxy controller for Gate Control Service
 * Routes gate control requests from API Gateway to Gate Control Service
 */
@RestController
@RequestMapping("/api/v1/gate")
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
        log.info("Proxying POST request to Gate Control Service: /api/v1/gate/entry");
        return proxyRequest(HttpMethod.POST, "/api/v1/gate/entry", entryData, request);
    }

    /**
     * Proxy POST request to open gate (exit)
     */
    @PostMapping("/exit")
    public ResponseEntity<?> openExitGate(@RequestBody String exitData, HttpServletRequest request) {
        log.info("Proxying POST request to Gate Control Service: /api/v1/gate/exit");
        return proxyRequest(HttpMethod.POST, "/api/v1/gate/exit", exitData, request);
    }

    /**
     * Proxy POST request to manual gate control
     */
    @PostMapping("/control")
    public ResponseEntity<?> manualGateControl(@RequestBody String controlData, HttpServletRequest request) {
        log.info("Proxying POST request to Gate Control Service: /api/v1/gate/control");
        return proxyRequest(HttpMethod.POST, "/api/v1/gate/control", controlData, request);
    }

    /**
     * Proxy GET request to fetch all gate events
     */
    @GetMapping("/events")
    public ResponseEntity<?> getAllGateEvents(HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/v1/gate/events");
        return proxyRequest(HttpMethod.GET, "/api/v1/gate/events", null, request);
    }

    /**
     * Proxy GET request to fetch gate event by ID
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<?> getGateEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/v1/gate/events/{}", id);
        return proxyRequest(HttpMethod.GET, "/api/v1/gate/events/" + id, null, request);
    }

    /**
     * Proxy GET request to fetch gate status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getGateStatus(HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/v1/gate/status");
        return proxyRequest(HttpMethod.GET, "/api/v1/gate/status", null, request);
    }

    /**
     * Proxy GET request to fetch client gate history
     */
    @GetMapping("/clients/{clientId}/history")
    public ResponseEntity<?> getClientGateHistory(@PathVariable Long clientId, HttpServletRequest request) {
        log.info("Proxying GET request to Gate Control Service: /api/v1/gate/clients/{}/history", clientId);
        return proxyRequest(HttpMethod.GET, "/api/v1/gate/clients/" + clientId + "/history", null, request);
    }

    /**
     * Generic proxy method for all HTTP methods
     */
    private ResponseEntity<?> proxyRequest(HttpMethod method, String path, String body,
                                          HttpServletRequest request) {
        try {
            log.info("Proxying request: {} {} to {}", method, path, GATE_SERVICE_URL + path);
            if (body != null) {
                log.info("Request body: {}", body);
            }

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
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Gate Control Service returned client error: {} - {}. Response: {}",
                e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            log.error("Gate Control Service returned server error: {} - {}. Response: {}",
                e.getStatusCode(), e.getMessage(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Gate Control Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Gate Control Service: " + e.getMessage());
        }
    }
}


