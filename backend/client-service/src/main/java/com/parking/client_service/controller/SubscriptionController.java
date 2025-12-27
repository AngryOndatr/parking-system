package com.parking.client_service.controller;

import com.parking.common.entity.Subscription;
import com.parking.client_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clients/{clientId}/subscriptions")
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSubscription(@PathVariable Long clientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🚀 [SUBSCRIPTION CONTROLLER] GET /api/clients/{}/subscriptions/check - User: {}", 
                clientId, auth != null ? auth.getName() : "anonymous");
        
        boolean hasActive = subscriptionService.hasActiveSubscription(clientId);
        Map<String, Object> response = new HashMap<>();
        response.put("clientId", clientId);
        response.put("hasActiveSubscription", hasActive);
        
        if (hasActive) {
            subscriptionService.getActiveSubscription(clientId).ifPresent(subscription -> {
                response.put("subscriptionId", subscription.getId());
                response.put("type", subscription.getType());
                response.put("startDate", subscription.getStartDate());
                response.put("endDate", subscription.getEndDate());
            });
        }
        
        log.info("✅ [SUBSCRIPTION CONTROLLER] Subscription check result: {}", hasActive);
        return ResponseEntity.ok(response);
    }
}
