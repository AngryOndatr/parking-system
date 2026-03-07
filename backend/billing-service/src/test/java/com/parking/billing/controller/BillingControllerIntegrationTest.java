package com.parking.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.parking.billing.config.TestJacksonConfig;
import com.parking.billing.controller.BillingController;
import com.parking.billing.entity.ParkingEvent;
import com.parking.billing.entity.Payment;
import com.parking.billing.mapper.BillingMapper;
import com.parking.billing.repository.ParkingEventRepository;
import com.parking.billing.repository.PaymentRepository;
import com.parking.billing.service.BillingService;
import com.parking.billing_service.generated.model.FeeCalculationRequest;
import com.parking.billing_service.generated.model.PaymentRequest;
import com.parking.billing_service.repository.TariffRepository;
import com.parking.common.entity.Tariff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BillingController.
 * Tests the /api/v1/billing/calculate endpoint with real database interactions.
 */
@SpringBootTest(classes = com.parking.billing_service.BillingServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestJacksonConfig.class)
@Transactional
@DisplayName("Billing Controller Integration Tests")
class BillingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkingEventRepository parkingEventRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @BeforeEach
    void setUp() {

        // Clean up before each test
        paymentRepository.deleteAll();
        parkingEventRepository.deleteAll();
        tariffRepository.deleteAll();

        // Setup test tariff
        Tariff tariff = new Tariff();
        tariff.setTariffType("ONE_TIME");
        tariff.setHourlyRate(new BigDecimal("50.00"));
        tariff.setDailyRate(null);
        tariff.setDescription("Standard hourly rate");
        tariff.setIsActive(true);
        tariffRepository.save(tariff);
    }

    @Test
    @DisplayName("POST /api/v1/billing/calculate - Success: Calculate fee for 2 hours")
    void calculateFee_Success_TwoHours() throws Exception {
        // Given: Parking event with 2 hours duration
        ParkingEvent event = createParkingEvent("TICKET-001", 2);
        parkingEventRepository.save(event);

        LocalDateTime exitTime = event.getEntryTime().plusHours(2);
        OffsetDateTime exitTimeOffset = exitTime.atOffset(ZoneOffset.UTC);

        FeeCalculationRequest request = new FeeCalculationRequest();
        request.setParkingEventId(event.getId());
        request.setEntryTime(event.getEntryTime().atOffset(ZoneOffset.UTC));
        request.setExitTime(exitTimeOffset);
        request.setTariffType(FeeCalculationRequest.TariffTypeEnum.ONE_TIME);
        request.setIsSubscriber(false);

        // When & Then: Calculate fee
        mockMvc.perform(post("/api/v1/billing/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingEventId").value(event.getId()))
                .andExpect(jsonPath("$.durationMinutes").value(120))
                .andExpect(jsonPath("$.baseFee").value(100.00))
                .andExpect(jsonPath("$.totalFee").value(100.00))
                .andExpect(jsonPath("$.tariffApplied").value("ONE_TIME"));
    }

    @Test
    @DisplayName("POST /api/v1/billing/calculate - Success: Calculate fee for 1.5 hours (rounds up)")
    void calculateFee_Success_OneAndHalfHours_RoundsUp() throws Exception {
        // Given: Parking event with 1.5 hours duration
        ParkingEvent event = createParkingEvent("TICKET-002", 0);
        parkingEventRepository.save(event);

        LocalDateTime exitTime = event.getEntryTime().plusHours(1).plusMinutes(30);
        OffsetDateTime exitTimeOffset = exitTime.atOffset(ZoneOffset.UTC);

        FeeCalculationRequest request = new FeeCalculationRequest();
        request.setParkingEventId(event.getId());
        request.setEntryTime(event.getEntryTime().atOffset(ZoneOffset.UTC));
        request.setExitTime(exitTimeOffset);
        request.setTariffType(FeeCalculationRequest.TariffTypeEnum.ONE_TIME);
        request.setIsSubscriber(false);

        // When & Then: Calculate fee (should round up to 2 hours = 100.00)
        mockMvc.perform(post("/api/v1/billing/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingEventId").value(event.getId()))
                .andExpect(jsonPath("$.durationMinutes").value(90))
                .andExpect(jsonPath("$.baseFee").value(100.00)) // 2 hours * 50.00
                .andExpect(jsonPath("$.totalFee").value(100.00));
    }

    @Test
    @DisplayName("POST /api/v1/billing/calculate - Error 404: Ticket not found")
    void calculateFee_Error404_TicketNotFound() throws Exception {
        // Given: Non-existent parking event
        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime nowOffset = now.atOffset(ZoneOffset.UTC);

        FeeCalculationRequest request = new FeeCalculationRequest();
        request.setParkingEventId(99999L); // Non-existent ID
        request.setEntryTime(nowOffset.minusHours(2));
        request.setExitTime(nowOffset);
        request.setTariffType(FeeCalculationRequest.TariffTypeEnum.ONE_TIME);
        request.setIsSubscriber(false);

        // When & Then: Should return 404
        mockMvc.perform(post("/api/v1/billing/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("Parking event not found")));
    }

    @Test
    @DisplayName("POST /api/v1/billing/calculate - Error 400: Ticket already paid")
    void calculateFee_Error400_TicketAlreadyPaid() throws Exception {
        // Given: Parking event with completed payment
        ParkingEvent event = createParkingEvent("TICKET-003", 2);
        parkingEventRepository.save(event);

        Payment payment = new Payment();
        payment.setParkingEventId(event.getId());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-TEST-001");
        paymentRepository.save(payment);

        LocalDateTime exitTime = event.getEntryTime().plusHours(2);
        OffsetDateTime exitTimeOffset = exitTime.atOffset(ZoneOffset.UTC);

        FeeCalculationRequest request = new FeeCalculationRequest();
        request.setParkingEventId(event.getId());
        request.setEntryTime(event.getEntryTime().atOffset(ZoneOffset.UTC));
        request.setExitTime(exitTimeOffset);
        request.setTariffType(FeeCalculationRequest.TariffTypeEnum.ONE_TIME);
        request.setIsSubscriber(false);

        // When & Then: Should return 400 (already paid)
        mockMvc.perform(post("/api/v1/billing/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("already paid")));
    }

    @Test
    @DisplayName("POST /api/v1/billing/pay - Success: Process payment")
    void processPayment_Success() throws Exception {
        // Given: Parking event without payment
        ParkingEvent event = createParkingEvent("TICKET-004", 3);
        parkingEventRepository.save(event);

        PaymentRequest request = new PaymentRequest();
        request.setParkingEventId(event.getId());
        request.setAmount(150.00); // 3 hours * 50.00
        request.setPaymentMethod(PaymentRequest.PaymentMethodEnum.CARD);

        // When & Then: Process payment
        mockMvc.perform(post("/api/v1/billing/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").exists())
                .andExpect(jsonPath("$.parkingEventId").value(event.getId()))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.transactionId").exists());
    }

    @Test
    @DisplayName("POST /api/v1/billing/pay - Error 400: Insufficient payment amount")
    void processPayment_Error400_InsufficientAmount() throws Exception {
        // Given: Parking event requiring 150.00 (3 hours)
        ParkingEvent event = createParkingEvent("TICKET-005", 3);
        parkingEventRepository.save(event);

        PaymentRequest request = new PaymentRequest();
        request.setParkingEventId(event.getId());
        request.setAmount(100.00); // Insufficient amount
        request.setPaymentMethod(PaymentRequest.PaymentMethodEnum.CASH);

        // When & Then: Should return 400
        mockMvc.perform(post("/api/v1/billing/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Insufficient payment")));
    }

    @Test
    @DisplayName("GET /api/v1/billing/status - Success: Check payment status")
    void getPaymentStatus_Success() throws Exception {
        // Given: Parking event with completed payment
        ParkingEvent event = createParkingEvent("TICKET-006", 2);
        parkingEventRepository.save(event);

        Payment payment = new Payment();
        payment.setParkingEventId(event.getId());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN-TEST-002");
        paymentRepository.save(payment);

        // When & Then: Check status
        mockMvc.perform(get("/api/v1/billing/status")
                        .param("parkingEventId", event.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingEventId").value(event.getId()))
                .andExpect(jsonPath("$.isPaid").value(true))
                .andExpect(jsonPath("$.remainingFee").value(0.0));
    }

    @Test
    @DisplayName("GET /api/v1/billing/status - Success: Check unpaid status with remaining fee")
    void getPaymentStatus_Unpaid_WithRemainingFee() throws Exception {
        // Given: Parking event without payment (2 hours parking = 100 UAH)
        ParkingEvent event = createParkingEvent("TICKET-007", 2);
        parkingEventRepository.save(event);

        // When & Then: Check status
        mockMvc.perform(get("/api/v1/billing/status")
                        .param("parkingEventId", event.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parkingEventId").value(event.getId()))
                .andExpect(jsonPath("$.isPaid").value(false))
                .andExpect(jsonPath("$.remainingFee").value(100.0));
    }

    @Test
    @DisplayName("GET /api/v1/billing/status - Error 404: Parking event not found")
    void getPaymentStatus_NotFound() throws Exception {
        // When & Then: Check status for non-existent event
        mockMvc.perform(get("/api/v1/billing/status")
                        .param("parkingEventId", "999999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // Helper method to create parking event
    private ParkingEvent createParkingEvent(String ticketCode, int hoursAgo) {
        ParkingEvent event = new ParkingEvent();
        event.setTicketCode(ticketCode);
        event.setLicensePlate("AA1234BB");
        event.setVehicleId(1L);
        event.setEntryTime(LocalDateTime.now().minusHours(hoursAgo));
        event.setEntryMethod(ParkingEvent.EntryMethod.SCAN);
        event.setSpotId(10L);
        event.setIsSubscriber(false);
        return event;
    }
}
