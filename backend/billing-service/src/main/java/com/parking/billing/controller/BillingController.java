package com.parking.billing.controller;

import com.parking.billing.entity.Payment;
import com.parking.billing.exception.ParkingEventNotFoundException;
import com.parking.billing.exception.TicketAlreadyPaidException;
import com.parking.billing.mapper.BillingMapper;
import com.parking.billing.service.BillingService;
import com.parking.billing_service.generated.api.BillingApi;
import com.parking.billing_service.generated.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    @Override
    public ResponseEntity<FeeCalculationResponse> calculateFee(FeeCalculationRequest feeCalculationRequest) {
        log.info("Received fee calculation request for parking event: {}",
                feeCalculationRequest.getParkingEventId());

        try {
            // For now, we use a simple ticket code pattern based on parking event ID
            // In real implementation, this should come from the parking event lookup
            String ticketCode = "TICKET-" + feeCalculationRequest.getParkingEventId();

            LocalDateTime exitTime = feeCalculationRequest.getExitTime()
                    .atZoneSameInstant(ZoneOffset.systemDefault())
                    .toLocalDateTime();

            BigDecimal calculatedFee = billingService.calculateFee(ticketCode, exitTime);

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
            // For now, we use a simple ticket code pattern based on parking event ID
            String ticketCode = "TICKET-" + paymentRequest.getParkingEventId();

            BigDecimal amount = BigDecimal.valueOf(paymentRequest.getAmount());
            Payment.PaymentMethod method = billingMapper.toPaymentMethod(paymentRequest.getPaymentMethod());
            Long operatorId = paymentRequest.getOperatorId().orElse(null);

            Payment payment = billingService.recordPayment(ticketCode, amount, method, operatorId);

            PaymentResponse response = billingMapper.toPaymentResponse(payment);

            log.info("Payment processed successfully. Transaction ID: {}", payment.getTransactionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ParkingEventNotFoundException e) {
            log.error("Parking event not found: {}", e.getMessage());
            throw e;
        } catch (TicketAlreadyPaidException e) {
            log.error("Ticket already paid: {}", e.getMessage());
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
            String ticketCode = "TICKET-" + parkingEventId;
            boolean isPaid = billingService.isTicketPaid(ticketCode);

            PaymentStatusResponse response = billingMapper.toPaymentStatusResponse(
                    parkingEventId,
                    isPaid
            );

            log.info("Payment status retrieved for event {}: {}", parkingEventId, isPaid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting payment status: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting payment status", e);
        }
    }
}

