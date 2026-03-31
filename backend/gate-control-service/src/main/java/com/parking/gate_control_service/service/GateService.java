package com.parking.gate_control_service.service;

import com.parking.gate_control_service.audit.AuditLogger;
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
import java.util.Map;
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
    private final AuditLogger auditLogger;

    private static final String GATE_ID_ENTRY = "ENTRY-1";
    private static final String ACTION_OPEN   = "OPEN";
    private static final String ACTION_DENY   = "DENY";

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
        Long subId = subscriptionCheck.getSubscriptionId();
        log.info("Subscriber entry plate={}, subscriptionId={}", licensePlate, subId);

        // Save gate event
        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.ENTRY);
        event.setLicensePlate(licensePlate);
        event.setGateId(GATE_ID_ENTRY);
        event.setDecision(GateEvent.Decision.OPEN);
        event.setReason("Valid subscription (ID: " + subId + ")");
        event.setTimestamp(LocalDateTime.now());
        GateEvent saved = gateEventRepository.save(event);

        auditLogger.audit("GATE_ENTRY", "GATE", saved.getId(), null, licensePlate,
                "Gate ENTRY opened for subscriber: plate=" + licensePlate + ", subscriptionId=" + subId,
                Map.of("gateId", GATE_ID_ENTRY, "subscriptionId", String.valueOf(subId), "decision", ACTION_OPEN));

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
        log.info("One-time visitor entry plate={}, ticketCode={}", licensePlate, ticketCode);

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

        auditLogger.audit("GATE_ENTRY", "GATE", saved.getId(), null, licensePlate,
                "Gate ENTRY opened for visitor: plate=" + licensePlate + ", ticket=" + ticketCode,
                Map.of("gateId", GATE_ID_ENTRY, "ticketCode", ticketCode, "decision", ACTION_OPEN));

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
        long timestamp  = System.currentTimeMillis();
        String random   = UUID.randomUUID().toString().substring(0, 8);
        return String.format("TICKET-%d-%s", timestamp, random);
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
            log.info("=== Processing exit plate={}, ticketCode={} ===", licensePlate, ticketCode);

            // Step 1: Check subscription
            log.debug("Step 1: Checking subscription for license plate: {}", licensePlate);
            SubscriptionCheckResponse subscriptionCheck = clientServiceClient.checkSubscription(licensePlate);
            log.debug("Subscription check result: isAccessGranted={}", subscriptionCheck.getIsAccessGranted());

            if (subscriptionCheck.getIsAccessGranted()) {
                // Subscriber exit - always allowed
                log.info("Subscriber detected, granting exit");
                GateEvent event = new GateEvent();
                event.setEventType(GateEvent.EventType.EXIT);
                event.setLicensePlate(licensePlate);
                event.setGateId("EXIT-1");
                event.setDecision(GateEvent.Decision.OPEN);
                event.setReason("Subscriber exit");
                event.setTimestamp(LocalDateTime.now());
                GateEvent saved = gateEventRepository.save(event);

                auditLogger.audit("GATE_EXIT", "GATE", saved.getId(), null, licensePlate,
                        "Gate EXIT opened for subscriber: plate=" + licensePlate,
                        Map.of("gateId", "EXIT-1", "decision", ACTION_OPEN));

                return ExitDecision.builder().action(ACTION_OPEN).message("Goodbye!").build();

            } else if (ticketCode != null && !ticketCode.isBlank()) {
                // One-time visitor: check payment by ticket code
                log.debug("Step 2: Checking payment status for ticket code: {}", ticketCode);

                PaymentStatusResponse paymentStatus = billingServiceClient.checkPaymentStatusByTicket(ticketCode);
                log.info("Payment status received for ticket {}: isPaid={}, remainingFee={}",
                    ticketCode,
                    paymentStatus != null ? paymentStatus.getIsPaid() : "null",
                    paymentStatus != null ? paymentStatus.getRemainingFee() : "null");

                if (paymentStatus != null && Boolean.TRUE.equals(paymentStatus.getIsPaid())) {
                    log.info("Payment confirmed, granting exit");
                    GateEvent event = new GateEvent();
                    event.setEventType(GateEvent.EventType.EXIT);
                    event.setLicensePlate(licensePlate);
                    event.setTicketCode(ticketCode);
                    event.setGateId("EXIT-1");
                    event.setDecision(GateEvent.Decision.OPEN);
                    event.setReason("Payment complete");
                    event.setTimestamp(LocalDateTime.now());
                    GateEvent saved = gateEventRepository.save(event);

                    auditLogger.audit("GATE_EXIT", "GATE", saved.getId(), null, licensePlate,
                            "Gate EXIT opened after payment: plate=" + licensePlate + ", ticket=" + ticketCode,
                            Map.of("gateId", "EXIT-1", "ticketCode", ticketCode, "decision", ACTION_OPEN));

                    return ExitDecision.builder().action(ACTION_OPEN).message("Thank you for your payment. Goodbye!").build();

                } else {
                    // Not paid
                    log.info("Payment not confirmed, denying exit");
                    GateEvent event = new GateEvent();
                    event.setEventType(GateEvent.EventType.EXIT);
                    event.setLicensePlate(licensePlate);
                    event.setTicketCode(ticketCode);
                    event.setGateId("EXIT-1");
                    event.setDecision(GateEvent.Decision.DENY);
                    event.setReason("Payment required");
                    event.setTimestamp(LocalDateTime.now());
                    GateEvent saved = gateEventRepository.save(event);

                    String feeMsg = paymentStatus != null && paymentStatus.getRemainingFee() != null
                            ? String.valueOf(paymentStatus.getRemainingFee()) : "unknown";
                    Long billingEventId = paymentStatus != null ? paymentStatus.getParkingEventId() : null;
                    java.math.BigDecimal remainingFee = paymentStatus != null ? paymentStatus.getRemainingFee() : null;

                    auditLogger.audit("GATE_EXIT_DENIED", "GATE", saved.getId(), null, licensePlate,
                            "Gate EXIT DENIED — payment required: plate=" + licensePlate + ", fee=" + feeMsg,
                            Map.of("gateId", "EXIT-1", "ticketCode", ticketCode, "decision", ACTION_DENY, "fee", feeMsg));

                    return ExitDecision.builder()
                            .action(ACTION_DENY)
                            .message("Payment required: " + feeMsg)
                            .parkingEventId(billingEventId)
                            .fee(remainingFee)
                            .build();
                }
            } else {
                // No ticket and not a subscriber
                log.warn("No ticket and not a subscriber plate={}", licensePlate);

                auditLogger.audit("GATE_EXIT_DENIED", "GATE", null, null, licensePlate,
                        "Gate EXIT DENIED — no ticket or subscription: plate=" + licensePlate,
                        Map.of("gateId", "EXIT-1", "decision", ACTION_DENY));

                return ExitDecision.builder().action(ACTION_DENY).message("No valid ticket or subscription found.").build();
            }
        } catch (Exception e) {
            log.error("Unexpected error processing exit plate={}, ticket={}: {}", licensePlate, ticketCode, e.getMessage(), e);
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
        log.info("Manual control: gateId={}, action={}, operatorId={}, reason={}", gateId, action, operatorId, reason);

        GateEvent event = new GateEvent();
        event.setEventType(GateEvent.EventType.MANUAL_OPEN);
        event.setLicensePlate("-");
        event.setGateId(gateId != null ? gateId : "UNKNOWN");
        event.setDecision("OPEN".equalsIgnoreCase(action) ? GateEvent.Decision.OPEN : GateEvent.Decision.DENY);
        event.setReason(reason != null ? reason : "Manual operator action");
        event.setTimestamp(LocalDateTime.now());
        event.setOperatorId(operatorId);
        GateEvent saved = gateEventRepository.save(event);

        auditLogger.audit("GATE_MANUAL_CONTROL", "GATE", saved.getId(), null, null,
                "Gate manual control: gateId=" + gateId + ", action=" + action + ", operatorId=" + operatorId,
                Map.of("gateId", gateId != null ? gateId : "UNKNOWN", "action", action != null ? action : "", "operatorId", String.valueOf(operatorId)));
    }
}
