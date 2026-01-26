package com.parking.gate_control_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the response from Client Service for subscription validation.
 * Contains information about whether access should be granted based on subscription status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCheckResponse {

    /**
     * Indicates whether the vehicle has access based on subscription.
     */
    private Boolean isAccessGranted;

    /**
     * Client ID if subscription exists (nullable).
     */
    private Long clientId;

    /**
     * Subscription ID if active subscription exists (nullable).
     */
    private Long subscriptionId;
}
