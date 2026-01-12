package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Proxy controller for Management Service
 * Routes management requests from API Gateway to Management Service
 */
@RestController
@RequestMapping("/api/management")
@RequiredArgsConstructor
@Slf4j
public class ManagementProxyController {

    private final RestTemplate restTemplate;
    private static final String MANAGEMENT_SERVICE_URL = "http://management-service:8083";

    /**
     * Proxy GET request to fetch all parking spots
     */
    @GetMapping("/spots")
    public ResponseEntity<?> getAllParkingSpots(HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots");
        return proxyRequest(HttpMethod.GET, "/api/management/spots", null, request);
    }

    /**
     * Proxy GET request to fetch parking spot by ID
     */
    @GetMapping("/spots/{id}")
    public ResponseEntity<?> getParkingSpotById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots/{}", id);
        return proxyRequest(HttpMethod.GET, "/api/management/spots/" + id, null, request);
    }

    /**
     * Proxy POST request to create a new parking spot
     */
    @PostMapping("/spots")
    public ResponseEntity<?> createParkingSpot(@RequestBody String spotData, HttpServletRequest request) {
        log.info("Proxying POST request to Management Service: /api/management/spots");
        return proxyRequest(HttpMethod.POST, "/api/management/spots", spotData, request);
    }

    /**
     * Proxy PUT request to update parking spot
     */
    @PutMapping("/spots/{id}")
    public ResponseEntity<?> updateParkingSpot(@PathVariable Long id, @RequestBody String spotData,
                                               HttpServletRequest request) {
        log.info("Proxying PUT request to Management Service: /api/management/spots/{}", id);
        return proxyRequest(HttpMethod.PUT, "/api/management/spots/" + id, spotData, request);
    }

    /**
     * Proxy DELETE request to delete parking spot
     */
    @DeleteMapping("/spots/{id}")
    public ResponseEntity<?> deleteParkingSpot(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying DELETE request to Management Service: /api/management/spots/{}", id);
        return proxyRequest(HttpMethod.DELETE, "/api/management/spots/" + id, null, request);
    }

    /**
     * Proxy GET request to fetch available spots count
     */
    @GetMapping("/spots/available/count")
    public ResponseEntity<?> getAvailableSpotCount(HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots/available/count");
        return proxyRequest(HttpMethod.GET, "/api/management/spots/available/count", null, request);
    }

    /**
     * Proxy GET request to fetch available spots by lot
     */
    @GetMapping("/spots/available/lot/{lotId}")
    public ResponseEntity<?> getAvailableSpotsByLot(@PathVariable Long lotId, HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots/available/lot/{}", lotId);
        return proxyRequest(HttpMethod.GET, "/api/management/spots/available/lot/" + lotId, null, request);
    }

    /**
     * Proxy GET request to fetch available spots
     */
    @GetMapping("/spots/available")
    public ResponseEntity<?> getAvailableSpots(HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots/available");
        return proxyRequest(HttpMethod.GET, "/api/management/spots/available", null, request);
    }

    /**
     * Proxy GET request to search spots by filters
     */
    @GetMapping("/spots/search")
    public ResponseEntity<?> searchSpots(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String status,
                                          HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots/search?type={}&status={}", type, status);
        String query = "";
        if (type != null || status != null) {
            query = "?";
            if (type != null) query += "type=" + type;
            if (type != null && status != null) query += "&";
            if (status != null) query += "status=" + status;
        }
        return proxyRequest(HttpMethod.GET, "/api/management/spots/search" + query, null, request);
    }

    /**
     * Proxy GET request to fetch occupied spots
     */
    @GetMapping("/spots/occupied")
    public ResponseEntity<?> getOccupiedSpots(HttpServletRequest request) {
        log.info("Proxying GET request to Management Service: /api/management/spots/occupied");
        return proxyRequest(HttpMethod.GET, "/api/management/spots/occupied", null, request);
    }

    /**
     * Proxy POST request to assign spot to client
     */
    @PostMapping("/spots/{spotId}/assign/{clientId}")
    public ResponseEntity<?> assignSpotToClient(@PathVariable Long spotId, @PathVariable Long clientId,
                                                HttpServletRequest request) {
        log.info("Proxying POST request to Management Service: /api/management/spots/{}/assign/{}",
                spotId, clientId);
        return proxyRequest(HttpMethod.POST,
                "/api/management/spots/" + spotId + "/assign/" + clientId, null, request);
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
                MANAGEMENT_SERVICE_URL + path,
                method,
                entity,
                String.class
            );

            log.info("Management Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Management Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Management Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Management Service: " + e.getMessage());
        }
    }
}

