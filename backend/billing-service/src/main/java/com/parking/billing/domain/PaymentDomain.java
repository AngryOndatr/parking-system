package com.parking.billing.domain;

import com.parking.billing.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model wrapping Payment entity.
 * Contains business logic for payment operations.
 */
@Getter
@AllArgsConstructor
public class PaymentDomain {

    private final Payment entity;

    /**
     * Factory method to create a new payment.
     *
     * @param parkingEventId ID of the parking event
     * @param amount payment amount
     * @param paymentMethod payment method
     * @param operatorId ID of the operator processing the payment
     * @return new PaymentDomain instance
     */
    public static PaymentDomain createPayment(
            Long parkingEventId,
            BigDecimal amount,
            Payment.PaymentMethod paymentMethod,
            Long operatorId
    ) {
        Payment payment = new Payment();
        payment.setParkingEventId(parkingEventId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setOperatorId(operatorId);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId(generateTransactionId());

        return new PaymentDomain(payment);
    }

    /**
     * Generate a unique transaction ID.
     *
     * @return unique transaction ID
     */
    private static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * Validate that the payment amount is sufficient.
     *
     * @param expectedAmount the expected amount
     * @throws IllegalArgumentException if amount is insufficient
     */
    public void validateAmount(BigDecimal expectedAmount) {
        if (expectedAmount == null) {
            throw new IllegalArgumentException("Expected amount cannot be null");
        }

        if (entity.getAmount().compareTo(expectedAmount) < 0) {
            throw new IllegalArgumentException(
                    String.format("Insufficient payment amount. Expected: %.2f, Received: %.2f",
                            expectedAmount, entity.getAmount())
            );
        }
    }

    /**
     * Check if payment is completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return Payment.PaymentStatus.COMPLETED.equals(entity.getStatus());
    }

    /**
     * Get the parking event ID.
     *
     * @return parking event ID
     */
    public Long getParkingEventId() {
        return entity.getParkingEventId();
    }

    /**
     * Get the payment amount.
     *
     * @return payment amount
     */
    public BigDecimal getAmount() {
        return entity.getAmount();
    }

    /**
     * Get the transaction ID.
     *
     * @return transaction ID
     */
    public String getTransactionId() {
        return entity.getTransactionId();
    }

    /**
     * Get the payment status.
     *
     * @return payment status
     */
    public Payment.PaymentStatus getStatus() {
        return entity.getStatus();
    }
}

