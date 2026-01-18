package com.parking.billing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a payment transaction.
 * Maps to payments table in the database.
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parking_event_id", nullable = false)
    @NotNull(message = "Parking event ID cannot be null")
    private Long parkingEventId;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be non-negative")
    private BigDecimal amount;

    @Column(name = "payment_time", nullable = false)
    @NotNull(message = "Payment time cannot be null")
    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @NotNull(message = "Payment status cannot be null")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enum representing payment methods.
     */
    public enum PaymentMethod {
        CARD,
        CASH,
        MOBILE_PAY
    }

    /**
     * Enum representing payment status.
     */
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }

    /**
     * Set default values before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (paymentTime == null) {
            paymentTime = LocalDateTime.now();
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }
}

