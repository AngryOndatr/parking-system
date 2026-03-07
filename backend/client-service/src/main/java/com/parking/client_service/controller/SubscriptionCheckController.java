package com.parking.client_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for subscription checks (used by Gate Control Service)
 * This is a simplified version for E2E tests - always returns no subscription
 */
@RestController
@RequestMapping("/api/v1/clients/subscriptions")
@Slf4j
public class SubscriptionCheckController {

    /**
     * Check if a vehicle has an active subscription
     * For E2E tests, this always returns false (no subscription)
     * so that the visitor flow is tested
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSubscription(
            @RequestParam String licensePlate) {

        log.info("🔍 [SUBSCRIPTION CHECK] Checking subscription for license plate: {}", licensePlate);

        Map<String, Object> response = new HashMap<>();
        response.put("isAccessGranted", false);
        response.put("licensePlate", licensePlate);
        response.put("subscriptionId", null);
        response.put("message", "No active subscription found");

        log.info("✅ [SUBSCRIPTION CHECK] No subscription for {}", licensePlate);

        return ResponseEntity.ok(response);
    }
}

