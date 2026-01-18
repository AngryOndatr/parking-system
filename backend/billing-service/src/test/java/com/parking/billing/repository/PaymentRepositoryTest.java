package com.parking.billing.repository;

import com.parking.billing.config.TestConfig;
import com.parking.billing.entity.Payment;
import com.parking.billing_service.BillingServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PaymentRepository.
 * Uses @DataJpaTest for lightweight JPA testing with in-memory database.
 */
@DataJpaTest
@ContextConfiguration(classes = {BillingServiceApplication.class})
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("Payment Repository Tests")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        testPayment = new Payment();
        testPayment.setParkingEventId(100L);
        testPayment.setAmount(new BigDecimal("50.00"));
        testPayment.setPaymentTime(LocalDateTime.now());
        testPayment.setPaymentMethod(Payment.PaymentMethod.CARD);
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setTransactionId("TXN-001");
        testPayment.setOperatorId(10L);
    }

    @Test
    @DisplayName("Should save and find payment by ID")
    void testSaveAndFindById() {
        // When
        Payment saved = paymentRepository.save(testPayment);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getPaymentTime()).isNotNull();

        Optional<Payment> found = paymentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should find payment by parking event ID and status")
    void testFindByParkingEventIdAndStatus() {
        // Given
        paymentRepository.save(testPayment);

        // When
        Optional<Payment> found = paymentRepository
                .findByParkingEventIdAndStatus(100L, Payment.PaymentStatus.COMPLETED);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTransactionId()).isEqualTo("TXN-001");
    }

    @Test
    @DisplayName("Should check if payment exists by parking event ID and status")
    void testExistsByParkingEventIdAndStatus() {
        // Given
        paymentRepository.save(testPayment);

        // When & Then
        assertThat(paymentRepository
                .existsByParkingEventIdAndStatus(100L, Payment.PaymentStatus.COMPLETED))
                .isTrue();

        assertThat(paymentRepository
                .existsByParkingEventIdAndStatus(100L, Payment.PaymentStatus.PENDING))
                .isFalse();

        assertThat(paymentRepository
                .existsByParkingEventIdAndStatus(999L, Payment.PaymentStatus.COMPLETED))
                .isFalse();
    }

    @Test
    @DisplayName("Should find payment by transaction ID")
    void testFindByTransactionId() {
        // Given
        paymentRepository.save(testPayment);

        // When
        Optional<Payment> found = paymentRepository.findByTransactionId("TXN-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getParkingEventId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should handle multiple payments for same parking event with different statuses")
    void testMultiplePaymentsForSameEvent() {
        // Given - PENDING payment
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        testPayment.setTransactionId("TXN-PENDING");
        paymentRepository.save(testPayment);

        // COMPLETED payment for same event
        Payment completedPayment = new Payment();
        completedPayment.setParkingEventId(100L);
        completedPayment.setAmount(new BigDecimal("50.00"));
        completedPayment.setPaymentTime(LocalDateTime.now());
        completedPayment.setPaymentMethod(Payment.PaymentMethod.CARD);
        completedPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        completedPayment.setTransactionId("TXN-COMPLETED");
        paymentRepository.save(completedPayment);

        // When & Then
        assertThat(paymentRepository
                .existsByParkingEventIdAndStatus(100L, Payment.PaymentStatus.PENDING))
                .isTrue();

        assertThat(paymentRepository
                .existsByParkingEventIdAndStatus(100L, Payment.PaymentStatus.COMPLETED))
                .isTrue();
    }

    @Test
    @DisplayName("Should store all payment method enums")
    void testPaymentMethodEnums() {
        // Test CARD
        testPayment.setPaymentMethod(Payment.PaymentMethod.CARD);
        Payment saved1 = paymentRepository.save(testPayment);
        assertThat(saved1.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CARD);

        // Test CASH
        testPayment.setId(null);
        testPayment.setTransactionId("TXN-002");
        testPayment.setPaymentMethod(Payment.PaymentMethod.CASH);
        Payment saved2 = paymentRepository.save(testPayment);
        assertThat(saved2.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CASH);

        // Test MOBILE_PAY
        testPayment.setId(null);
        testPayment.setTransactionId("TXN-003");
        testPayment.setPaymentMethod(Payment.PaymentMethod.MOBILE_PAY);
        Payment saved3 = paymentRepository.save(testPayment);
        assertThat(saved3.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.MOBILE_PAY);
    }

    @Test
    @DisplayName("Should store all payment status enums")
    void testPaymentStatusEnums() {
        // Test PENDING
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        testPayment.setTransactionId("TXN-PENDING");
        Payment saved1 = paymentRepository.save(testPayment);
        assertThat(saved1.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);

        // Test COMPLETED
        testPayment.setId(null);
        testPayment.setTransactionId("TXN-COMPLETED");
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        Payment saved2 = paymentRepository.save(testPayment);
        assertThat(saved2.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);

        // Test FAILED
        testPayment.setId(null);
        testPayment.setTransactionId("TXN-FAILED");
        testPayment.setStatus(Payment.PaymentStatus.FAILED);
        Payment saved3 = paymentRepository.save(testPayment);
        assertThat(saved3.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);

        // Test REFUNDED
        testPayment.setId(null);
        testPayment.setTransactionId("TXN-REFUNDED");
        testPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        Payment saved4 = paymentRepository.save(testPayment);
        assertThat(saved4.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Should handle payment amounts correctly")
    void testPaymentAmounts() {
        // Test zero amount
        testPayment.setAmount(BigDecimal.ZERO);
        Payment saved1 = paymentRepository.save(testPayment);
        assertThat(saved1.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        // Test large amount
        testPayment.setId(null);
        testPayment.setTransactionId("TXN-LARGE");
        testPayment.setAmount(new BigDecimal("9999.99"));
        Payment saved2 = paymentRepository.save(testPayment);
        assertThat(saved2.getAmount()).isEqualByComparingTo(new BigDecimal("9999.99"));
    }

    @Test
    @DisplayName("Should set default values on persist")
    void testPrePersistDefaults() {
        // Given - payment without createdAt and paymentTime
        Payment payment = new Payment();
        payment.setParkingEventId(200L);
        payment.setAmount(new BigDecimal("30.00"));
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-DEFAULT");

        // When
        Payment saved = paymentRepository.save(payment);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getPaymentTime()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should update payment status")
    void testUpdatePaymentStatus() {
        // Given
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        Payment saved = paymentRepository.save(testPayment);
        assertThat(saved.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);

        // When - update to COMPLETED
        saved.setStatus(Payment.PaymentStatus.COMPLETED);
        Payment updated = paymentRepository.save(saved);

        // Then
        assertThat(updated.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }
}

