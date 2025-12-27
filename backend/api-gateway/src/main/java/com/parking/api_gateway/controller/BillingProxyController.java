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
                    .headers(response.getHeaders())
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

