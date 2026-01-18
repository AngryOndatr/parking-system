package com.parking.billing.service;

import com.parking.billing.entity.Payment;
import com.parking.billing.entity.ParkingEvent;
import com.parking.billing.exception.ParkingEventNotFoundException;
import com.parking.billing.exception.TariffNotFoundException;
import com.parking.billing.exception.TicketAlreadyPaidException;
import com.parking.billing.repository.ParkingEventRepository;
import com.parking.billing.repository.PaymentRepository;
import com.parking.billing_service.repository.TariffRepository;
import com.parking.common.entity.Tariff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillingService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillingService Tests")
class BillingServiceTest {

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private ParkingEventRepository parkingEventRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BillingService billingService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @Captor
    private ArgumentCaptor<ParkingEvent> parkingEventCaptor;

    private Tariff testTariff;
    private ParkingEvent testParkingEvent;

    @BeforeEach
    void setUp() {
        // Setup test tariff
        testTariff = new Tariff();
        testTariff.setId(1L);
        testTariff.setTariffType("ONE_TIME");
        testTariff.setHourlyRate(new BigDecimal("5.00"));
        testTariff.setIsActive(true);

        // Setup test parking event
        testParkingEvent = new ParkingEvent();
        testParkingEvent.setId(1L);
        testParkingEvent.setTicketCode("TICKET-001");
        testParkingEvent.setLicensePlate("ABC123");
        testParkingEvent.setEntryTime(LocalDateTime.of(2026, 1, 18, 10, 0));
        testParkingEvent.setIsSubscriber(false);
    }

    @Test
    @DisplayName("Calculate fee for 1 hour parking")
    void calculateFee_1Hour_ReturnsCorrectFee() {
        // Given
        String ticketCode = "TICKET-001";
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 11, 0); // Exactly 1 hour

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));

        // When
        BigDecimal fee = billingService.calculateFee(ticketCode, exitTime);

        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("5.00"));
        verify(parkingEventRepository, atLeastOnce()).findByTicketCode(ticketCode);
        verify(tariffRepository).findByTariffTypeAndIsActiveTrue("ONE_TIME");
    }

    @Test
    @DisplayName("Calculate fee for 3 hours parking")
    void calculateFee_3Hours_ReturnsCorrectFee() {
        // Given
        String ticketCode = "TICKET-001";
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 13, 0); // Exactly 3 hours

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));

        // When
        BigDecimal fee = billingService.calculateFee(ticketCode, exitTime);

        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("Calculate fee for 1.5 hours - rounds up to 2 hours")
    void calculateFee_1Point5Hours_RoundsUpTo2Hours() {
        // Given
        String ticketCode = "TICKET-001";
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 11, 30); // 1.5 hours

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));

        // When
        BigDecimal fee = billingService.calculateFee(ticketCode, exitTime);

        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("10.00")); // 2 hours * 5.00
    }

    @Test
    @DisplayName("Calculate fee for 30 minutes - rounds up to 1 hour")
    void calculateFee_30Minutes_RoundsUpTo1Hour() {
        // Given
        String ticketCode = "TICKET-001";
        LocalDateTime exitTime = LocalDateTime.of(2026, 1, 18, 10, 30); // 30 minutes

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));

        // When
        BigDecimal fee = billingService.calculateFee(ticketCode, exitTime);

        // Then
        assertThat(fee).isEqualByComparingTo(new BigDecimal("5.00")); // Minimum 1 hour
    }

    @Test
    @DisplayName("Calculate fee throws exception when parking event not found")
    void calculateFee_ParkingEventNotFound_ThrowsException() {
        // Given
        String ticketCode = "INVALID-TICKET";
        LocalDateTime exitTime = LocalDateTime.now();

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> billingService.calculateFee(ticketCode, exitTime))
                .isInstanceOf(ParkingEventNotFoundException.class)
                .hasMessageContaining(ticketCode);
    }

    @Test
    @DisplayName("Calculate fee throws exception when ticket already paid")
    void calculateFee_TicketAlreadyPaid_ThrowsException() {
        // Given
        String ticketCode = "TICKET-001";
        LocalDateTime exitTime = LocalDateTime.now();

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(testParkingEvent.getId(),
                Payment.PaymentStatus.COMPLETED))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> billingService.calculateFee(ticketCode, exitTime))
                .isInstanceOf(TicketAlreadyPaidException.class)
                .hasMessageContaining(ticketCode);
    }

    @Test
    @DisplayName("Calculate fee throws exception when tariff not found")
    void calculateFee_TariffNotFound_ThrowsException() {
        // Given
        String ticketCode = "TICKET-001";
        LocalDateTime exitTime = LocalDateTime.now();

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> billingService.calculateFee(ticketCode, exitTime))
                .isInstanceOf(TariffNotFoundException.class)
                .hasMessageContaining("ONE_TIME");
    }

    @Test
    @DisplayName("Record payment successfully")
    void recordPayment_ValidData_Success() {
        // Given
        String ticketCode = "TICKET-001";
        BigDecimal amountPaid = new BigDecimal("15.00");
        Payment.PaymentMethod method = Payment.PaymentMethod.CARD;
        Long operatorId = 100L;

        // Set entry time to 3 hours ago to match the payment amount
        testParkingEvent.setEntryTime(LocalDateTime.now().minusHours(3));

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));
        when(parkingEventRepository.save(any(ParkingEvent.class)))
                .thenReturn(testParkingEvent);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setId(1L);
                    return payment;
                });

        // When
        Payment result = billingService.recordPayment(ticketCode, amountPaid, method, operatorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getParkingEventId()).isEqualTo(testParkingEvent.getId());
        assertThat(result.getAmount()).isEqualByComparingTo(amountPaid);
        assertThat(result.getPaymentMethod()).isEqualTo(method);
        assertThat(result.getOperatorId()).isEqualTo(operatorId);
        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(result.getTransactionId()).isNotNull();

        verify(parkingEventRepository).save(parkingEventCaptor.capture());
        ParkingEvent updatedEvent = parkingEventCaptor.getValue();
        assertThat(updatedEvent.getExitTime()).isNotNull();
    }

    @Test
    @DisplayName("Record payment with exact amount")
    void recordPayment_ExactAmount_Success() {
        // Given
        String ticketCode = "TICKET-001";
        testParkingEvent.setEntryTime(LocalDateTime.now().minusHours(2));
        BigDecimal expectedFee = new BigDecimal("10.00"); // 2 hours * 5.00
        Payment.PaymentMethod method = Payment.PaymentMethod.CASH;
        Long operatorId = 100L;

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));
        when(parkingEventRepository.save(any(ParkingEvent.class)))
                .thenReturn(testParkingEvent);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Payment result = billingService.recordPayment(ticketCode, expectedFee, method, operatorId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(expectedFee);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Record payment with insufficient amount throws exception")
    void recordPayment_InsufficientAmount_ThrowsException() {
        // Given
        String ticketCode = "TICKET-001";
        BigDecimal insufficientAmount = new BigDecimal("5.00");
        testParkingEvent.setEntryTime(LocalDateTime.now().minusHours(3)); // Should cost 15.00

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);
        when(tariffRepository.findByTariffTypeAndIsActiveTrue("ONE_TIME"))
                .thenReturn(Optional.of(testTariff));

        // When / Then
        assertThatThrownBy(() -> billingService.recordPayment(
                ticketCode, insufficientAmount, Payment.PaymentMethod.CARD, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient payment amount");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Record payment for already paid ticket throws exception")
    void recordPayment_AlreadyPaid_ThrowsException() {
        // Given
        String ticketCode = "TICKET-001";
        BigDecimal amount = new BigDecimal("10.00");

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(
                testParkingEvent.getId(), Payment.PaymentStatus.COMPLETED))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> billingService.recordPayment(
                ticketCode, amount, Payment.PaymentMethod.CARD, 100L))
                .isInstanceOf(TicketAlreadyPaidException.class)
                .hasMessageContaining(ticketCode);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Check ticket paid returns true when payment exists")
    void isTicketPaid_PaymentExists_ReturnsTrue() {
        // Given
        String ticketCode = "TICKET-001";

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(
                testParkingEvent.getId(), Payment.PaymentStatus.COMPLETED))
                .thenReturn(true);

        // When
        boolean result = billingService.isTicketPaid(ticketCode);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Check ticket paid returns false when no payment exists")
    void isTicketPaid_NoPayment_ReturnsFalse() {
        // Given
        String ticketCode = "TICKET-001";

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.of(testParkingEvent));
        when(paymentRepository.existsByParkingEventIdAndStatus(anyLong(), any()))
                .thenReturn(false);

        // When
        boolean result = billingService.isTicketPaid(ticketCode);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Check ticket paid returns false when ticket not found")
    void isTicketPaid_TicketNotFound_ReturnsFalse() {
        // Given
        String ticketCode = "INVALID-TICKET";

        when(parkingEventRepository.findByTicketCode(ticketCode))
                .thenReturn(Optional.empty());

        // When
        boolean result = billingService.isTicketPaid(ticketCode);

        // Then
        assertThat(result).isFalse();
        verify(paymentRepository, never()).existsByParkingEventIdAndStatus(anyLong(), any());
    }
}

