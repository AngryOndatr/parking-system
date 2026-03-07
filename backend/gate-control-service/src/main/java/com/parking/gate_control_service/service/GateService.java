package com.parking.gate_control_service.service;

import com.parking.gate_control_service.client.BillingServiceClient;
import com.parking.gate_control_service.client.ClientServiceClient;
import com.parking.gate_control_service.dto.EntryDecision;
import com.parking.gate_control_service.dto.ExitDecision;
import com.parking.gate_control_service.dto.PaymentStatusResponse;
import com.parking.gate_control_service.dto.SubscriptionCheckResponse;
import com.parking.gate_control_service.entity.GateEvent;
import com.parking.gate_control_service.repository.GateEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for gate operation logic.
 * Handles entry and exit decisions based on subscription and payment status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GateService {

    private final ClientServiceClient clientServiceClient;
    private final BillingServiceClient billingServiceClient;
    private final GateEventRepository gateEventRepository;

    private static final String GATE_ID_ENTRY = "ENTRY-1";
    private static final String ACTION_OPEN = "OPEN";
    private static final String ACTION_DENY = "DENY";

    /**
     * Process vehicle entry request.
     * Checks subscription status and makes decision to open gate or deny entry.
     *
     * @param licensePlate the vehicle's license plate
     * @return EntryDecision with action, message, and optional ticket code
     */
    @Transactional
    public EntryDecision processEntry(String licensePlate) {
        log.info("Processing entry for license plate: {}", licensePlate);

        // Check subscription status
        SubscriptionCheckResponse subscriptionCheck = clientServiceClient.checkSubscription(licensePlate);

        if (subscriptionCheck.getIsAccessGranted()) {
            // Subscriber path - grant access without ticket
            return processSubscriberEntry(licensePlate, subscriptionCheck);
        } else {
            // One-time visitor path - generate ticket and grant access
            return processVisitorEntry(licensePlate);
        }
    }

    /**
     * Process entry for subscriber with active subscription.
     *
     * @param licensePlate      the vehicle's license plate
     * @param subscriptionCheck the subscription check response
     * @return EntryDecision with OPEN action
     */
    private EntryDecision processSubscriberEntry(String licensePlate, SubscriptionCheckResponse subscriptionCheck) {
        log.info("Subscriber entry for license plate: {}, subscription ID: {}",
                licensePlate, subscriptionCheck.getSubscriptionId());

        // Save gate event
        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.ENTRY);
        event.setLicensePlate(licensePlate);
        event.setGateId(GATE_ID_ENTRY);
        event.setDecision(GateEvent.Decision.OPEN);
        event.setReason("Valid subscription (ID: " + subscriptionCheck.getSubscriptionId() + ")");
        event.setTimestamp(LocalDateTime.now());
        GateEvent saved = gateEventRepository.save(event);

        return EntryDecision.builder()
                .parkingEventId(saved.getId())
                .action(ACTION_OPEN)
                .message("Welcome, subscriber!")
                .build();
    }

    /**
     * Process entry for one-time visitor without subscription.
     * Generates a unique ticket code.
     *
     * @param licensePlate the vehicle's license plate
     * @return EntryDecision with OPEN action and ticket code
     */
    private EntryDecision processVisitorEntry(String licensePlate) {
        // Generate unique ticket code
        String ticketCode = generateTicketCode();
        log.info("One-time visitor entry for license plate: {}, ticket code: {}", licensePlate, ticketCode);

        // Save gate event
        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.ENTRY);
        event.setLicensePlate(licensePlate);
        event.setTicketCode(ticketCode);
        event.setGateId(GATE_ID_ENTRY);
        event.setDecision(GateEvent.Decision.OPEN);
        event.setReason("Ticket issued");
        event.setTimestamp(LocalDateTime.now());
        GateEvent saved = gateEventRepository.save(event);

        return EntryDecision.builder()
                .parkingEventId(saved.getId())
                .action(ACTION_OPEN)
                .message("Take your ticket")
                .ticketCode(ticketCode)
                .build();
    }

    /**
     * Generate a unique ticket code.
     * Format: TICKET-{timestamp}-{random}
     *
     * @return generated ticket code
     */
    private String generateTicketCode() {
        long timestamp = System.currentTimeMillis();
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        return String.format("TICKET-%d-%s", timestamp, randomPart);
    }

    /**
     * Process vehicle exit request.
     * Checks if the vehicle is a subscriber or if payment is completed for one-time visitors.
     *
     * @param ticketCode   the ticket code (nullable for subscribers)
     * @param licensePlate the vehicle's license plate
     * @return ExitDecision with action and message
     */
    @Transactional
    public ExitDecision processExit(String ticketCode, String licensePlate) {
        try {
            log.info("=== Processing exit for license plate: {}, ticketCode: {} ===", licensePlate, ticketCode);

            // Step 1: Check subscription
            log.debug("Step 1: Checking subscription for license plate: {}", licensePlate);
            SubscriptionCheckResponse subscriptionCheck = clientServiceClient.checkSubscription(licensePlate);
            log.debug("Subscription check result: isAccessGranted={}", subscriptionCheck.getIsAccessGranted());

            if (subscriptionCheck.getIsAccessGranted()) {
                // Subscriber exit - always allowed
                log.info("Subscriber detected, granting access");
                GateEvent event = new GateEvent();
                event.setEventType(GateEvent.EventType.EXIT);
                event.setLicensePlate(licensePlate);
                event.setGateId("EXIT-1");
                event.setDecision(GateEvent.Decision.OPEN);
                event.setReason("Subscriber exit");
                event.setTimestamp(LocalDateTime.now());
                gateEventRepository.save(event);
                return ExitDecision.builder()
                        .action(ACTION_OPEN)
                        .message("Goodbye!")
                        .build();
            } else if (ticketCode != null && !ticketCode.isBlank()) {
                // One-time visitor: check payment by ticket code
                log.debug("Step 2: Checking payment status for ticket code: {}", ticketCode);

                PaymentStatusResponse paymentStatus = billingServiceClient.checkPaymentStatusByTicket(ticketCode);
                log.info("Payment status received for ticket {}: isPaid={}, remainingFee={}",
                    ticketCode,
                    paymentStatus != null ? paymentStatus.getIsPaid() : "null",
                    paymentStatus != null ? paymentStatus.getRemainingFee() : "null");

                if (paymentStatus != null && Boolean.TRUE.equals(paymentStatus.getIsPaid())) {
                    log.info("Payment confirmed, granting access");
                    GateEvent event = new GateEvent();
                    event.setEventType(GateEvent.EventType.EXIT);
                    event.setLicensePlate(licensePlate);
                    event.setTicketCode(ticketCode);
                    event.setGateId("EXIT-1");
                    event.setDecision(GateEvent.Decision.OPEN);
                    event.setReason("Payment complete");
                    event.setTimestamp(LocalDateTime.now());
                    gateEventRepository.save(event);
                    return ExitDecision.builder()
                            .action(ACTION_OPEN)
                            .message("Thank you for your payment. Goodbye!")
                            .build();
                } else {
                    // Not paid
                    log.info("Payment not confirmed, denying access");
                    GateEvent event = new GateEvent();
                    event.setEventType(GateEvent.EventType.EXIT);
                    event.setLicensePlate(licensePlate);
                    event.setTicketCode(ticketCode);
                    event.setGateId("EXIT-1");
                    event.setDecision(GateEvent.Decision.DENY);
                    event.setReason("Payment required");
                    event.setTimestamp(LocalDateTime.now());
                gateEventRepository.save(event);
                String feeMsg = paymentStatus != null && paymentStatus.getRemainingFee() != null
                        ? String.valueOf(paymentStatus.getRemainingFee())
                        : "unknown";
                return ExitDecision.builder()
                        .action(ACTION_DENY)
                        .message("Payment required: " + feeMsg)
                        .build();
            }
        } else {
            // No ticket and not a subscriber
            log.warn("No ticket code provided and not a subscriber");
            return ExitDecision.builder()
                    .action(ACTION_DENY)
                    .message("No valid ticket or subscription found.")
                    .build();
        }
        } catch (Exception e) {
            log.error("Unexpected error processing exit for licensePlate={}, ticketCode={}: {}",
                licensePlate, ticketCode, e.getMessage(), e);
            throw new RuntimeException("Error processing exit", e);
        }
    }

    /**
     * Process a manual control action performed by an operator.
     * Saves a MANUAL_OPEN GateEvent and records operator information.
     *
     * @param gateId     gate identifier
     * @param action     action performed (OPEN/CLOSE)
     * @param operatorId operator id who performed the action
     * @param reason     optional reason text
     */
    @Transactional
    public void processManualControl(String gateId, String action, Long operatorId, String reason) {
        log.info("Processing manual control: gateId={}, action={}, operatorId={}, reason={}", gateId, action, operatorId, reason);

        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.MANUAL_OPEN);
        event.setLicensePlate("-"); // no license plate for manual control
        event.setGateId(gateId != null ? gateId : "UNKNOWN");
        // for manual controls, decision OPEN when action equals OPEN, otherwise DENY
        event.setDecision("OPEN".equalsIgnoreCase(action) ? GateEvent.Decision.OPEN : GateEvent.Decision.DENY);
        event.setReason(reason != null ? reason : "Manual operator action");
        event.setTimestamp(LocalDateTime.now());
        event.setOperatorId(operatorId);

        gateEventRepository.save(event);
    }
}
