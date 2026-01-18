package com.parking.billing.service;

import com.parking.billing.domain.ParkingEventDomain;
import com.parking.billing.domain.PaymentDomain;
import com.parking.billing.entity.Payment;
import com.parking.billing.entity.ParkingEvent;
import com.parking.billing.exception.ParkingEventNotFoundException;
import com.parking.billing.exception.TariffNotFoundException;
import com.parking.billing.exception.TicketAlreadyPaidException;
import com.parking.billing.repository.ParkingEventRepository;
import com.parking.billing.repository.PaymentRepository;
import com.parking.billing_service.repository.TariffRepository;
import com.parking.common.entity.Tariff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service for billing operations.
 * Handles fee calculations and payment processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private static final String ONE_TIME_TARIFF = "ONE_TIME";

    private final TariffRepository tariffRepository;
    private final ParkingEventRepository parkingEventRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Calculate parking fee for a ticket.
     *
     * @param ticketCode the ticket code
     * @param exitTime the exit time
     * @return calculated fee
     * @throws ParkingEventNotFoundException if parking event not found
     * @throws TicketAlreadyPaidException if ticket is already paid
     * @throws TariffNotFoundException if ONE_TIME tariff not found
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(String ticketCode, LocalDateTime exitTime) {
        log.info("Calculating fee for ticket: {} with exit time: {}", ticketCode, exitTime);

        // Find parking event
        ParkingEvent parkingEvent = parkingEventRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ParkingEventNotFoundException(ticketCode));

        // Check if already paid
        if (isTicketPaid(ticketCode)) {
            log.warn("Ticket already paid: {}", ticketCode);
            throw new TicketAlreadyPaidException(ticketCode);
        }

        // Wrap in domain model
        ParkingEventDomain domain = new ParkingEventDomain(parkingEvent);

        // Get ONE_TIME tariff
        Tariff tariff = tariffRepository.findByTariffTypeAndIsActiveTrue(ONE_TIME_TARIFF)
                .orElseThrow(() -> new TariffNotFoundException(ONE_TIME_TARIFF));

        BigDecimal hourlyRate = tariff.getHourlyRate();

        // Calculate fee using domain logic
        BigDecimal fee = domain.calculateFee(exitTime, hourlyRate);

        log.info("Calculated fee for ticket {}: {} (Duration: {} hours, Rate: {}/hour)",
                ticketCode, fee, domain.calculateDurationInHours(exitTime), hourlyRate);

        return fee;
    }

    /**
     * Record a payment for a ticket.
     *
     * @param ticketCode the ticket code
     * @param amountPaid the amount paid
     * @param method the payment method
     * @param operatorId the operator ID
     * @return the recorded payment entity
     * @throws ParkingEventNotFoundException if parking event not found
     * @throws TicketAlreadyPaidException if ticket is already paid
     * @throws IllegalArgumentException if amount is insufficient
     */
    @Transactional
    public Payment recordPayment(
            String ticketCode,
            BigDecimal amountPaid,
            Payment.PaymentMethod method,
            Long operatorId
    ) {
        log.info("Recording payment for ticket: {} amount: {} method: {} operator: {}",
                ticketCode, amountPaid, method, operatorId);

        // Find parking event
        ParkingEvent parkingEvent = parkingEventRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ParkingEventNotFoundException(ticketCode));

        // Check if already paid
        if (isTicketPaid(ticketCode)) {
            log.warn("Ticket already paid: {}", ticketCode);
            throw new TicketAlreadyPaidException(ticketCode);
        }

        // Calculate expected fee
        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal expectedFee = calculateFee(ticketCode, exitTime);

        // Create payment using domain model
        PaymentDomain paymentDomain = PaymentDomain.createPayment(
                parkingEvent.getId(),
                amountPaid,
                method,
                operatorId
        );

        // Validate amount
        try {
            paymentDomain.validateAmount(expectedFee);
        } catch (IllegalArgumentException e) {
            log.error("Payment validation failed for ticket {}: {}", ticketCode, e.getMessage());
            throw e;
        }

        // Update parking event exit time
        parkingEvent.setExitTime(exitTime);
        parkingEventRepository.save(parkingEvent);

        // Save payment
        Payment savedPayment = paymentRepository.save(paymentDomain.getEntity());

        log.info("Payment recorded successfully. Transaction ID: {} for ticket: {}",
                savedPayment.getTransactionId(), ticketCode);

        return savedPayment;
    }

    /**
     * Check if a ticket has been paid.
     *
     * @param ticketCode the ticket code
     * @return true if paid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTicketPaid(String ticketCode) {
        log.debug("Checking payment status for ticket: {}", ticketCode);

        ParkingEvent parkingEvent = parkingEventRepository.findByTicketCode(ticketCode)
                .orElse(null);

        if (parkingEvent == null) {
            return false;
        }

        boolean isPaid = paymentRepository.existsByParkingEventIdAndStatus(
                parkingEvent.getId(),
                Payment.PaymentStatus.COMPLETED
        );

        log.debug("Ticket {} payment status: {}", ticketCode, isPaid ? "PAID" : "UNPAID");

        return isPaid;
    }
}

