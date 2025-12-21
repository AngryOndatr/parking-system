package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Proxy controller for Reporting Service
 * Routes reporting requests from API Gateway to Reporting Service
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportingProxyController {

    private final RestTemplate restTemplate;
    private static final String REPORTING_SERVICE_URL = "http://reporting-service:8080";

    /**
     * Proxy GET request to fetch financial report
     */
    @GetMapping("/financial")
    public ResponseEntity<?> getFinancialReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        log.info("Proxying GET request to Reporting Service: /api/reports/financial");

        String queryParams = buildQueryParams(startDate, endDate);
        return proxyRequest(HttpMethod.GET, "/api/reports/financial" + queryParams, null, request);
    }

    /**
     * Proxy GET request to fetch occupancy report
     */
    @GetMapping("/occupancy")
    public ResponseEntity<?> getOccupancyReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        log.info("Proxying GET request to Reporting Service: /api/reports/occupancy");

        String queryParams = buildQueryParams(startDate, endDate);
        return proxyRequest(HttpMethod.GET, "/api/reports/occupancy" + queryParams, null, request);
    }

    /**
     * Proxy GET request to fetch client usage report
     */
    @GetMapping("/clients/{clientId}/usage")
    public ResponseEntity<?> getClientUsageReport(
            @PathVariable Long clientId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        log.info("Proxying GET request to Reporting Service: /api/reports/clients/{}/usage", clientId);

        String queryParams = buildQueryParams(startDate, endDate);
        return proxyRequest(HttpMethod.GET,
                "/api/reports/clients/" + clientId + "/usage" + queryParams, null, request);
    }

    /**
     * Proxy GET request to fetch revenue report
     */
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueReport(
            @RequestParam(required = false) String period,
            HttpServletRequest request) {
        log.info("Proxying GET request to Reporting Service: /api/reports/revenue");

        String queryParams = period != null ? "?period=" + period : "";
        return proxyRequest(HttpMethod.GET, "/api/reports/revenue" + queryParams, null, request);
    }

    /**
     * Proxy GET request to fetch parking duration statistics
     */
    @GetMapping("/parking-duration")
    public ResponseEntity<?> getParkingDurationReport(HttpServletRequest request) {
        log.info("Proxying GET request to Reporting Service: /api/reports/parking-duration");
        return proxyRequest(HttpMethod.GET, "/api/reports/parking-duration", null, request);
    }

    /**
     * Proxy POST request to generate custom report
     */
    @PostMapping("/custom")
    public ResponseEntity<?> generateCustomReport(@RequestBody String reportConfig,
                                                  HttpServletRequest request) {
        log.info("Proxying POST request to Reporting Service: /api/reports/custom");
        return proxyRequest(HttpMethod.POST, "/api/reports/custom", reportConfig, request);
    }

    /**
     * Proxy GET request to export report
     */
    @GetMapping("/export/{reportId}")
    public ResponseEntity<?> exportReport(@PathVariable Long reportId,
                                         @RequestParam(defaultValue = "pdf") String format,
                                         HttpServletRequest request) {
        log.info("Proxying GET request to Reporting Service: /api/reports/export/{}?format={}",
                reportId, format);
        return proxyRequest(HttpMethod.GET,
                "/api/reports/export/" + reportId + "?format=" + format, null, request);
    }

    /**
     * Build query parameters string
     */
    private String buildQueryParams(String startDate, String endDate) {
        StringBuilder params = new StringBuilder();
        boolean hasParams = false;

        if (startDate != null && !startDate.isEmpty()) {
            params.append("?startDate=").append(startDate);
            hasParams = true;
        }

        if (endDate != null && !endDate.isEmpty()) {
            params.append(hasParams ? "&" : "?").append("endDate=").append(endDate);
        }

        return params.toString();
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
                REPORTING_SERVICE_URL + path,
                method,
                entity,
                String.class
            );

            log.info("Reporting Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Reporting Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Reporting Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Reporting Service: " + e.getMessage());
        }
    }
}

