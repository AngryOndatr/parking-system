package com.parking.billing.controller;

import com.parking.billing.entity.Payment;
import com.parking.billing.entity.ParkingEvent;
import com.parking.billing.exception.ParkingEventNotFoundException;
import com.parking.billing.exception.TicketAlreadyPaidException;
import com.parking.billing.mapper.BillingMapper;
import com.parking.billing.service.BillingService;
import com.parking.billing.repository.PaymentRepository;
import com.parking.billing.repository.ParkingEventRepository;
import com.parking.billing_service.generated.api.BillingApi;
import com.parking.billing_service.generated.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller implementing BillingApi for fee calculation and payment processing.
 * Handles OpenAPI-first contract implementation.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class BillingController implements BillingApi {

    private final BillingService billingService;
    private final BillingMapper billingMapper;
    private final PaymentRepository paymentRepository;
    private final ParkingEventRepository parkingEventRepository;

    @Override
    public ResponseEntity<FeeCalculationResponse> calculateFee(FeeCalculationRequest feeCalculationRequest) {
        log.info("Received fee calculation request for parking event: {}",
                feeCalculationRequest.getParkingEventId());

        try {
            LocalDateTime entryTime = feeCalculationRequest.getEntryTime()
                    .atZoneSameInstant(ZoneOffset.systemDefault())
                    .toLocalDateTime();

            LocalDateTime exitTime = feeCalculationRequest.getExitTime()
                    .atZoneSameInstant(ZoneOffset.systemDefault())
                    .toLocalDateTime();

            BigDecimal calculatedFee = billingService.calculateFeeByEventIdWithTimes(
                    feeCalculationRequest.getParkingEventId(),
                    entryTime,
                    exitTime
            );

            FeeCalculationResponse response = billingMapper.toFeeCalculationResponse(
                    feeCalculationRequest.getParkingEventId(),
                    feeCalculationRequest.getEntryTime(),
                    exitTime,
                    calculatedFee,
                    feeCalculationRequest.getTariffType().getValue()
            );

            log.info("Fee calculation successful for event {}: {}",
                    feeCalculationRequest.getParkingEventId(), calculatedFee);

            return ResponseEntity.ok(response);

        } catch (ParkingEventNotFoundException e) {
            log.error("Parking event not found: {}", e.getMessage());
            throw e;
        } catch (TicketAlreadyPaidException e) {
            log.error("Ticket already paid: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error calculating fee: {}", e.getMessage(), e);
            throw new RuntimeException("Error calculating fee", e);
        }
    }

    @Override
    public ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        log.info("Received payment request for parking event: {} amount: {} method: {}",
                paymentRequest.getParkingEventId(),
                paymentRequest.getAmount(),
                paymentRequest.getPaymentMethod());

        try {
            BigDecimal amount = BigDecimal.valueOf(paymentRequest.getAmount());
            Payment.PaymentMethod method = billingMapper.toPaymentMethod(paymentRequest.getPaymentMethod());
            Long operatorId = paymentRequest.getOperatorId().orElse(null);

            Payment payment = billingService.recordPaymentByEventId(
                    paymentRequest.getParkingEventId(),
                    amount,
                    method,
                    operatorId
            );

            PaymentResponse response = billingMapper.toPaymentResponse(payment);

            log.info("Payment processed successfully. Transaction ID: {}", payment.getTransactionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ParkingEventNotFoundException e) {
            log.error("Parking event not found: {}", e.getMessage());
            throw e;
        } catch (TicketAlreadyPaidException e) {
            log.error("Ticket already paid: {}", e.getMessage());
            throw e;
        } catch (com.parking.billing.exception.InsufficientPaymentException e) {
            log.error("Insufficient payment: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing payment", e);
        }
    }

    @Override
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(Long parkingEventId) {
        log.info("Received payment status request for parking event: {}", parkingEventId);

        try {
            boolean isPaid = billingService.isEventPaid(parkingEventId);
            BigDecimal remainingFee = billingService.getRemainingFee(parkingEventId);

            PaymentStatusResponse response = billingMapper.toPaymentStatusResponse(
                    parkingEventId,
                    isPaid,
                    remainingFee
            );

            log.info("Payment status retrieved for event {}: paid={}, remainingFee={}",
                    parkingEventId, isPaid, remainingFee);

            return ResponseEntity.ok(response);

        } catch (ParkingEventNotFoundException e) {
            log.warn("Parking event {} not found", parkingEventId);
            throw e;
        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting payment status", e);
        }
    }

    /**
     * Get payment status by ticket code instead of parking event ID.
     * This is useful for gate-control-service which only knows the ticket code.
     *
     * @param ticketCode the ticket code
     * @return payment status response
     */
    @org.springframework.web.bind.annotation.GetMapping("/api/v1/billing/status-by-ticket")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatusByTicket(
            @org.springframework.web.bind.annotation.RequestParam String ticketCode) {
        log.info("Received payment status request for ticket: {}", ticketCode);

        try {
            // Find ParkingEvent by ticket code
            ParkingEvent parkingEvent = parkingEventRepository.findByTicketCode(ticketCode)
                    .orElseThrow(() -> new ParkingEventNotFoundException("Parking event not found for ticket: " + ticketCode));

            boolean isPaid = billingService.isEventPaid(parkingEvent.getId());
            BigDecimal remainingFee = billingService.getRemainingFee(parkingEvent.getId());

            PaymentStatusResponse response = billingMapper.toPaymentStatusResponse(
                    parkingEvent.getId(),
                    isPaid,
                    remainingFee
            );

            log.info("Payment status retrieved for ticket {}: paid={}, remainingFee={}",
                    ticketCode, isPaid, remainingFee);

            return ResponseEntity.ok(response);

        } catch (ParkingEventNotFoundException e) {
            log.warn("Parking event not found for ticket {}, returning unpaid status", ticketCode);
            // Return unpaid status for unknown tickets
            PaymentStatusResponse response = new PaymentStatusResponse();
            response.setParkingEventId(0L); // Dummy ID
            response.setIsPaid(false);
            response.setRemainingFee(org.openapitools.jackson.nullable.JsonNullable.of(0.0));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment status by ticket: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting payment status", e);
        }
    }

    /**
     * Simplified payment endpoint for E2E testing.
     * Creates ParkingEvent if needed and saves payment to database.
     * Now accepts ticketCode instead of parkingEventId to properly link with GateEvent.
     *
     * @param request payment request with ticketCode, licensePlate and amount
     * @return payment response
     */
    @PostMapping("/api/v1/billing/pay-test")
    public ResponseEntity<PaymentResponse> processTestPayment(@RequestBody Map<String, Object> request) {
        log.info("🧪 TEST ENDPOINT: Received simplified payment request: {}", request);

        try {
            String ticketCode = (String) request.get("ticketCode");
            String licensePlate = (String) request.get("licensePlate");
            Double amount = ((Number) request.get("amount")).doubleValue();

            log.info("Creating test payment for ticketCode: {}, licensePlate: {}, amount: {}",
                    ticketCode, licensePlate, amount);

            // Find or create ParkingEvent by ticket code
            ParkingEvent parkingEvent = parkingEventRepository.findByTicketCode(ticketCode)
                    .orElseGet(() -> {
                        log.info("ParkingEvent not found for ticket {}, creating new one", ticketCode);
                        ParkingEvent newEvent = new ParkingEvent();
                        // Don't set ID - let database generate it
                        newEvent.setVehicleId(null); // nullable for one-time visitors
                        newEvent.setLicensePlate(licensePlate != null ? licensePlate : "E2E-TEST");
                        newEvent.setTicketCode(ticketCode);
                        newEvent.setEntryTime(LocalDateTime.now().minusHours(2));
                        newEvent.setIsSubscriber(false);
                        newEvent.setEntryMethod(ParkingEvent.EntryMethod.SCAN);
                        ParkingEvent saved = parkingEventRepository.save(newEvent);
                        log.info("✅ Created ParkingEvent with ID: {} for ticket: {}", saved.getId(), ticketCode);
                        return saved;
                    });

            // Create Payment
            Payment payment = new Payment();
            payment.setParkingEventId(parkingEvent.getId());
            payment.setAmount(BigDecimal.valueOf(amount));
            payment.setPaymentMethod(Payment.PaymentMethod.CARD);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentTime(LocalDateTime.now());
            payment.setTransactionId("TEST-" + System.currentTimeMillis());

            log.info("Saving payment with transaction ID: {}", payment.getTransactionId());
            Payment saved = paymentRepository.save(payment);

            log.info("✅ Test payment saved successfully with ID: {}, parkingEventId: {}",
                    saved.getId(), parkingEvent.getId());

            PaymentResponse response = billingMapper.toPaymentResponse(saved);
            // Also return parkingEventId in response for compatibility
            response.setParkingEventId(parkingEvent.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Error in test payment endpoint: {}", e.getMessage(), e);
            throw new RuntimeException("Test payment failed: " + e.getMessage(), e);
        }
    }

    /**
     * Test endpoint to create a ParkingEvent without payment.
     * Useful for UI to request tariff calculation without paying.
     *
     * @param request contains ticketCode, licensePlate, and optional entryMinutesAgo
     * @return created parking event details
     */
    @PostMapping("/api/v1/billing/test-event")
    public ResponseEntity<Map<String, Object>> createTestParkingEvent(@RequestBody Map<String, Object> request) {
        log.info("🧪 TEST ENDPOINT: Preparing parking event: {}", request);
        try {
            String ticketCode = (String) request.getOrDefault("ticketCode", "TEST-" + System.currentTimeMillis());
            String licensePlate = (String) request.getOrDefault("licensePlate", "E2E-TEST");
            int entryMinutesAgo = request.containsKey("entryMinutesAgo")
                    ? Math.max(((Number) request.get("entryMinutesAgo")).intValue(), 0)
                    : 120;

            ParkingEvent parkingEvent = parkingEventRepository.findByTicketCode(ticketCode)
                    .orElseGet(() -> {
                        ParkingEvent event = new ParkingEvent();
                        event.setTicketCode(ticketCode);
                        event.setLicensePlate(licensePlate);
                        event.setEntryTime(LocalDateTime.now().minusMinutes(entryMinutesAgo));
                        event.setIsSubscriber(false);
                        event.setEntryMethod(ParkingEvent.EntryMethod.SCAN);
                        return parkingEventRepository.save(event);
                    });

            Map<String, Object> response = new HashMap<>();
            response.put("parkingEventId", parkingEvent.getId());
            response.put("ticketCode", parkingEvent.getTicketCode());
            response.put("licensePlate", parkingEvent.getLicensePlate());
            response.put("entryTime", parkingEvent.getEntryTime());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("❌ Error creating test parking event: {}", e.getMessage(), e);
            throw new RuntimeException("Test parking event creation failed: " + e.getMessage(), e);
        }
    }
}

