package com.parking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Proxy controller for Billing Service
 * Routes billing-related requests from API Gateway to Billing Service
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingProxyController {

    private final RestTemplate restTemplate;
    private static final String BILLING_SERVICE_URL = "http://billing-service:8080";

    /**
     * Proxy POST request to calculate parking fee
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateFee(@RequestBody String calculateData, HttpServletRequest request) {
        log.info("Proxying POST request to Billing Service: /api/billing/calculate");
        return proxyRequest(HttpMethod.POST, "/api/billing/calculate", calculateData, request);
    }

    /**
     * Proxy POST request to process payment
     */
    @PostMapping("/pay")
    public ResponseEntity<?> processPayment(@RequestBody String paymentData, HttpServletRequest request) {
        log.info("Proxying POST request to Billing Service: /api/billing/pay");
        return proxyRequest(HttpMethod.POST, "/api/billing/pay", paymentData, request);
    }

    /**
     * Proxy POST request to test payment endpoint (for E2E tests)
     */
    @PostMapping("/pay-test")
    public ResponseEntity<?> processTestPayment(@RequestBody String paymentData, HttpServletRequest request) {
        log.info("🧪 Proxying POST request to Billing Service TEST endpoint: /api/billing/pay-test");
        return proxyRequest(HttpMethod.POST, "/api/billing/pay-test", paymentData, request);
    }

    /**
     * Proxy POST request to create unpaid parking event for billing quick tests
     */
    @PostMapping("/test-event")
    public ResponseEntity<?> createTestEvent(@RequestBody String eventData, HttpServletRequest request) {
        log.info("Proxying POST request to Billing Service TEST endpoint: /api/billing/test-event");
        return proxyRequest(HttpMethod.POST, "/api/billing/test-event", eventData, request);
    }

    /**
     * Proxy GET request to check payment status
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkPaymentStatus(
            @RequestParam(required = false) String ticketCode,
            @RequestParam(required = false) Long parkingEventId,
            HttpServletRequest request) {

        // Build query string based on provided parameters
        StringBuilder queryString = new StringBuilder();
        if (ticketCode != null) {
            queryString.append("ticketCode=").append(ticketCode);
        } else if (parkingEventId != null) {
            queryString.append("parkingEventId=").append(parkingEventId);
        }

        String fullPath = "/api/billing/status" + (queryString.length() > 0 ? "?" + queryString : "");
        log.info("Proxying GET request to Billing Service: {}", fullPath);
        return proxyRequest(HttpMethod.GET, fullPath, null, request);
    }

    /**
     * Proxy GET request to fetch all invoices
     */
    @GetMapping("/invoices")
    public ResponseEntity<?> getAllInvoices(HttpServletRequest request) {
        log.info("Proxying GET request to Billing Service: /api/billing/invoices");
        return proxyRequest(HttpMethod.GET, "/api/billing/invoices", null, request);
    }

    /**
     * Proxy GET request to fetch invoice by ID
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoiceById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying GET request to Billing Service: /api/billing/invoices/{}", id);
        return proxyRequest(HttpMethod.GET, "/api/billing/invoices/" + id, null, request);
    }

    /**
     * Proxy POST request to create a new invoice
     */
    @PostMapping("/invoices")
    public ResponseEntity<?> createInvoice(@RequestBody String invoiceData, HttpServletRequest request) {
        log.info("Proxying POST request to Billing Service: /api/billing/invoices");
        return proxyRequest(HttpMethod.POST, "/api/billing/invoices", invoiceData, request);
    }

    /**
     * Proxy PUT request to update an invoice
     */
    @PutMapping("/invoices/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id, @RequestBody String invoiceData,
                                           HttpServletRequest request) {
        log.info("Proxying PUT request to Billing Service: /api/billing/invoices/{}", id);
        return proxyRequest(HttpMethod.PUT, "/api/billing/invoices/" + id, invoiceData, request);
    }

    /**
     * Proxy DELETE request to delete an invoice
     */
    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id, HttpServletRequest request) {
        log.info("Proxying DELETE request to Billing Service: /api/billing/invoices/{}", id);
        return proxyRequest(HttpMethod.DELETE, "/api/billing/invoices/" + id, null, request);
    }


    /**
     * Proxy GET request to fetch client invoices
     */
    @GetMapping("/clients/{clientId}/invoices")
    public ResponseEntity<?> getClientInvoices(@PathVariable Long clientId, HttpServletRequest request) {
        log.info("Proxying GET request to Billing Service: /api/billing/clients/{}/invoices", clientId);
        return proxyRequest(HttpMethod.GET, "/api/billing/clients/" + clientId + "/invoices", null, request);
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
                BILLING_SERVICE_URL + path,
                method,
                entity,
                String.class
            );

            log.info("Billing Service responded with status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode())
                    .headers(ProxyUtils.filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("Billing Service returned error: {} - {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error proxying request to Billing Service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error communicating with Billing Service: " + e.getMessage());
        }
    }
}
