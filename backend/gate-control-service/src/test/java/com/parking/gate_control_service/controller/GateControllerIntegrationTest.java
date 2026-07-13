package com.parking.gate_control_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.gate_control_service.client.BillingServiceClient;
import com.parking.gate_control_service.client.ClientServiceClient;
import com.parking.gate_control_service.config.TestJacksonConfig;
import com.parking.gate_control_service.config.TestSecurityConfig;
import com.parking.gate_control_service.dto.PaymentStatusResponse;
import com.parking.gate_control_service.dto.SubscriptionCheckResponse;
import com.parking.gate_control_service.generated.model.*;
import com.parking.gate_control_service.repository.GateEventRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GateController.
 * Tests the /api/gate/entry endpoint with mocked external dependencies.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({TestSecurityConfig.class, TestJacksonConfig.class})
@DisplayName("Gate Controller Integration Tests")
class GateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientServiceClient clientServiceClient;

    @MockBean
    private BillingServiceClient billingServiceClient;

    @Autowired
    private GateEventRepository gateEventRepository;

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/entry - Subscriber entry returns 201 with no ticket")
    void processEntry_Subscriber_Returns201WithoutTicket() throws Exception {
        // Given: Vehicle with active subscription
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(true)
                .clientId(100L)
                .subscriptionId(200L)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);

        EntryRequest request = new EntryRequest();
        request.setLicensePlate("ABC-123");
        request.setEntryMethod(EntryRequest.EntryMethodEnum.SCAN);
        request.setGateId("ENTRY-1");

        // When & Then: Entry processed successfully
        mockMvc.perform(post("/api/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.isSubscriber").value(true))
                .andExpect(jsonPath("$.ticketCode").doesNotExist())
                .andExpect(jsonPath("$.message").value("Welcome, subscriber!"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"))
                .andExpect(jsonPath("$.entryTime").exists())
                .andExpect(jsonPath("$.parkingEventId").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/entry - One-time visitor returns 201 with ticket")
    void processEntry_OneTimeVisitor_Returns201WithTicket() throws Exception {
        // Given: Vehicle without subscription
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);

        EntryRequest request = new EntryRequest();
        request.setLicensePlate("XYZ-789");
        request.setEntryMethod(EntryRequest.EntryMethodEnum.SCAN);
        request.setGateId("ENTRY-1");

        // When & Then: Entry processed successfully with ticket
        mockMvc.perform(post("/api/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("XYZ-789"))
                .andExpect(jsonPath("$.isSubscriber").value(false))
                .andExpect(jsonPath("$.ticketCode").exists())
                .andExpect(jsonPath("$.ticketCode").value(startsWith("TICKET-")))
                .andExpect(jsonPath("$.message").value("Take your ticket"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"))
                .andExpect(jsonPath("$.entryTime").exists())
                .andExpect(jsonPath("$.parkingEventId").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/entry - Invalid license plate returns 400")
    void processEntry_InvalidLicensePlate_Returns400() throws Exception {
        // Given: Invalid license plate (lowercase letters not allowed)
        EntryRequest request = new EntryRequest();
        request.setLicensePlate("abc-123"); // lowercase not allowed per OpenAPI spec pattern
        request.setEntryMethod(EntryRequest.EntryMethodEnum.SCAN);
        request.setGateId("ENTRY-1");

        // When & Then: Validation error
        mockMvc.perform(post("/api/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/entry - Missing required fields returns 400")
    void processEntry_MissingRequiredFields_Returns400() throws Exception {
        // Given: Request missing required fields
        EntryRequest request = new EntryRequest();
        // licensePlate, entryMethod, gateId are required but not set

        // When & Then: Validation error
        mockMvc.perform(post("/api/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/entry - Manual entry with operator ID succeeds")
    void processEntry_ManualEntryWithOperator_Returns201() throws Exception {
        // Given: Manual entry by operator
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);

        EntryRequest request = new EntryRequest();
        request.setLicensePlate("MAN-001");
        request.setEntryMethod(EntryRequest.EntryMethodEnum.MANUAL);
        request.setGateId("ENTRY-2");
        request.setOperatorId(JsonNullable.of(3L));

        // When & Then: Entry processed successfully
        mockMvc.perform(post("/api/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("MAN-001"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"));
    }

    // =================================================================
    // EXIT ENDPOINT TESTS
    // =================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/exit - Subscriber exit returns 200 with gate opened")
    void processExit_Subscriber_Returns200WithGateOpened() throws Exception {
        // Given: Vehicle with active subscription
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(true)
                .clientId(100L)
                .subscriptionId(200L)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);

        ExitRequest request = new ExitRequest();
        request.setLicensePlate(JsonNullable.of("ABC-123"));
        request.setTicketCode(JsonNullable.undefined());
        request.setExitMethod(ExitRequest.ExitMethodEnum.SCAN);
        request.setGateId("EXIT-1");

        // When & Then: Exit processed successfully
        mockMvc.perform(post("/api/gate/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC-123"))
                .andExpect(jsonPath("$.message").value("Goodbye!"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"))
                .andExpect(jsonPath("$.exitTime").exists())
                .andExpect(jsonPath("$.entryTime").exists())
                .andExpect(jsonPath("$.durationMinutes").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/exit - Paid one-time visitor returns 200 with gate opened")
    void processExit_PaidVisitor_Returns200WithGateOpened() throws Exception {
        // Given: Vehicle without subscription but with paid ticket
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        PaymentStatusResponse paymentStatus = PaymentStatusResponse.builder()
                .isPaid(true)
                .remainingFee(BigDecimal.ZERO)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);
        when(billingServiceClient.checkPaymentStatusByTicket(anyString())).thenReturn(paymentStatus);

        ExitRequest request = new ExitRequest();
        request.setLicensePlate(JsonNullable.of("XYZ-789"));
        request.setTicketCode(JsonNullable.of("TICKET-123456-abcd"));
        request.setExitMethod(ExitRequest.ExitMethodEnum.SCAN);
        request.setGateId("EXIT-1");

        // When & Then: Exit processed successfully
        mockMvc.perform(post("/api/gate/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("XYZ-789"))
                .andExpect(jsonPath("$.message").value("Thank you for your payment. Goodbye!"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"))
                .andExpect(jsonPath("$.exitTime").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/exit - Unpaid one-time visitor returns 200 with payment required")
    void processExit_UnpaidVisitor_Returns200WithPaymentRequired() throws Exception {
        // Given: Vehicle without subscription and unpaid ticket
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        PaymentStatusResponse paymentStatus = PaymentStatusResponse.builder()
                .isPaid(false)
                .remainingFee(new BigDecimal("25.50"))
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);
        when(billingServiceClient.checkPaymentStatusByTicket(anyString())).thenReturn(paymentStatus);

        ExitRequest request = new ExitRequest();
        request.setLicensePlate(JsonNullable.of("UNP-456"));
        request.setTicketCode(JsonNullable.of("TICKET-999999-efgh"));
        request.setExitMethod(ExitRequest.ExitMethodEnum.SCAN);
        request.setGateId("EXIT-1");

        // When & Then: Exit denied, payment required
        mockMvc.perform(post("/api/gate/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("UNP-456"))
                .andExpect(jsonPath("$.message").value(containsString("Payment required")))
                .andExpect(jsonPath("$.message").value(containsString("25.50")))
                .andExpect(jsonPath("$.paymentRequired").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/exit - No ticket and no subscription returns 200 with deny")
    void processExit_NoTicketNoSubscription_Returns200WithDeny() throws Exception {
        // Given: Vehicle without subscription and without ticket
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);

        ExitRequest request = new ExitRequest();
        request.setLicensePlate(JsonNullable.of("NON-000"));
        request.setTicketCode(JsonNullable.undefined());
        request.setExitMethod(ExitRequest.ExitMethodEnum.SCAN);
        request.setGateId("EXIT-1");

        // When & Then: Exit denied - no valid ticket or subscription
        mockMvc.perform(post("/api/gate/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("No valid ticket or subscription")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/exit - Manual exit by operator succeeds")
    void processExit_ManualByOperator_Returns200() throws Exception {
        // Given: Manual exit by operator for subscriber
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(true)
                .clientId(50L)
                .subscriptionId(100L)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(subscriptionResponse);

        ExitRequest request = new ExitRequest();
        request.setLicensePlate(JsonNullable.of("OPR-001"));
        request.setTicketCode(JsonNullable.undefined());
        request.setExitMethod(ExitRequest.ExitMethodEnum.MANUAL);
        request.setGateId("EXIT-2");
        request.setOperatorId(JsonNullable.of(5L));

        // When & Then: Exit processed successfully
        mockMvc.perform(post("/api/gate/exit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("OPR-001"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"));
    }

    // =================================================================
    // MANUAL CONTROL ENDPOINT TESTS
    // =================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/control - Open gate returns 200")
    void manualControl_OpenGate_Returns200() throws Exception {
        // Given: Operator opens gate manually
        GateControlRequest request = new GateControlRequest();
        request.setGateId("GATE-EXIT-1");
        request.setAction(GateControlRequest.ActionEnum.OPEN);
        request.setOperatorId(3L);
        request.setReason("Emergency exit - fire alarm");

        // When & Then: Gate opened successfully
        mockMvc.perform(post("/api/gate/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateId").value("GATE-EXIT-1"))
                .andExpect(jsonPath("$.action").value("OPEN"))
                .andExpect(jsonPath("$.status").value("OPENED"))
                .andExpect(jsonPath("$.operatorId").value(3))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Emergency exit - fire alarm"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/control - Close gate returns 200")
    void manualControl_CloseGate_Returns200() throws Exception {
        // Given: Operator closes gate for maintenance
        GateControlRequest request = new GateControlRequest();
        request.setGateId("GATE-ENTRY-1");
        request.setAction(GateControlRequest.ActionEnum.CLOSE);
        request.setOperatorId(7L);
        request.setReason("Maintenance mode");

        // When & Then: Gate control executed successfully
        mockMvc.perform(post("/api/gate/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateId").value("GATE-ENTRY-1"))
                .andExpect(jsonPath("$.action").value("CLOSE"))
                .andExpect(jsonPath("$.status").value("OPENED"))
                .andExpect(jsonPath("$.operatorId").value(7))
                .andExpect(jsonPath("$.message").value("Maintenance mode"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/control - Missing required fields returns 400")
    void manualControl_MissingRequiredFields_Returns400() throws Exception {
        // Given: Request missing required fields
        GateControlRequest request = new GateControlRequest();
        // gateId, action, operatorId, reason are required but not set

        // When & Then: Validation error
        mockMvc.perform(post("/api/gate/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/gate/control - Saves gate event in database")
    void manualControl_OpenGate_SavesGateEvent() throws Exception {
        // Given: Operator opens gate
        GateControlRequest request = new GateControlRequest();
        request.setGateId("GATE-ENTRY-2");
        request.setAction(GateControlRequest.ActionEnum.OPEN);
        request.setOperatorId(10L);
        request.setReason("VIP access");

        long eventCountBefore = gateEventRepository.count();

        // When: Manual control executed
        mockMvc.perform(post("/api/gate/control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // Then: New gate event is saved
        long eventCountAfter = gateEventRepository.count();
        assertThat(eventCountAfter).isGreaterThan(eventCountBefore);
    }
}
