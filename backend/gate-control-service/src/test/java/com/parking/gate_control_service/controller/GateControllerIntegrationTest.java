package com.parking.gate_control_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.gate_control_service.client.ClientServiceClient;
import com.parking.gate_control_service.config.TestJacksonConfig;
import com.parking.gate_control_service.config.TestSecurityConfig;
import com.parking.gate_control_service.dto.SubscriptionCheckResponse;
import com.parking.gate_control_service.generated.model.EntryRequest;
import com.parking.gate_control_service.repository.GateEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GateController.
 * Tests the /api/v1/gate/entry endpoint with mocked external dependencies.
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

    @MockitoBean
    private ClientServiceClient clientServiceClient;

    @Autowired
    private GateEventRepository gateEventRepository;

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/gate/entry - Subscriber entry returns 201 with no ticket")
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
        mockMvc.perform(post("/api/v1/gate/entry")
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
    @DisplayName("POST /api/v1/gate/entry - One-time visitor returns 201 with ticket")
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
        mockMvc.perform(post("/api/v1/gate/entry")
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
    @DisplayName("POST /api/v1/gate/entry - Invalid license plate returns 400")
    void processEntry_InvalidLicensePlate_Returns400() throws Exception {
        // Given: Invalid license plate (lowercase letters not allowed)
        EntryRequest request = new EntryRequest();
        request.setLicensePlate("abc-123"); // lowercase not allowed per OpenAPI spec pattern
        request.setEntryMethod(EntryRequest.EntryMethodEnum.SCAN);
        request.setGateId("ENTRY-1");

        // When & Then: Validation error
        mockMvc.perform(post("/api/v1/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/gate/entry - Missing required fields returns 400")
    void processEntry_MissingRequiredFields_Returns400() throws Exception {
        // Given: Request missing required fields
        EntryRequest request = new EntryRequest();
        // licensePlate, entryMethod, gateId are required but not set

        // When & Then: Validation error
        mockMvc.perform(post("/api/v1/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/gate/entry - Manual entry with operator ID succeeds")
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
        mockMvc.perform(post("/api/v1/gate/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licensePlate").value("MAN-001"))
                .andExpect(jsonPath("$.gateStatus").value("OPENED"));
    }
}
