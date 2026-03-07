package com.parking.billing.repository;

import com.parking.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Payment entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by parking event ID and status.
     * Useful for checking if a specific payment status exists for a parking event.
     *
     * @param eventId parking event ID
     * @param status  payment status
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByParkingEventIdAndStatus(Long eventId, Payment.PaymentStatus status);

    /**
     * Check if a payment exists for a parking event with a specific status.
     * Useful for validation (e.g., preventing duplicate COMPLETED payments).
     *
     * @param eventId parking event ID
     * @param status  payment status
     * @return true if exists, false otherwise
     */
    boolean existsByParkingEventIdAndStatus(Long eventId, Payment.PaymentStatus status);

    /**
     * Find payment by transaction ID.
     * Useful for tracking and reconciliation.
     *
     * @param transactionId unique transaction identifier
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByTransactionId(String transactionId);
}

