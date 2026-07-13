package com.parking.client_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.GlobalExceptionHandler;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.SubscriptionRequest;
import com.parking.client_service.generated.model.SubscriptionResponse;
import com.parking.client_service.security.JwtTokenProvider;
import com.parking.client_service.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionController MockMvc tests")
class SubscriptionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private SubscriptionService subscriptionService;
    @Mock private JwtTokenProvider    jwtTokenProvider;  // unused here, satisfies Spring context

    @InjectMocks
    private SubscriptionController subscriptionController;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new JsonNullableModule());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────

    private SubscriptionRequest validRequest() {
        SubscriptionRequest req = new SubscriptionRequest();
        req.setType(SubscriptionRequest.TypeEnum.MONTHLY);
        req.setStartDate(OffsetDateTime.now(ZoneOffset.UTC));
        req.setEndDate(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1));
        return req;
    }

    private SubscriptionResponse sampleResponse(Long id, Long clientId) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(id);
        r.setClientId(clientId);
        r.setType("MONTHLY");
        r.setIsActive(true);
        r.setStartDate(OffsetDateTime.now(ZoneOffset.UTC));
        r.setEndDate(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1));
        return r;
    }

    // ── POST /api/clients/{clientId}/subscriptions ─────────────

    @Test
    @DisplayName("POST /api/clients/1/subscriptions — 201 Created")
    void createSubscription_created() throws Exception {
        SubscriptionResponse response = sampleResponse(5L, 1L);
        when(subscriptionService.createSubscription(eq(1L), any(SubscriptionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/clients/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.type").value("MONTHLY"));
    }

    @Test
    @DisplayName("POST — missing required field type → 400 Bad Request")
    void createSubscription_missingType_400() throws Exception {
        SubscriptionRequest invalid = new SubscriptionRequest();
        // type is missing (required)
        invalid.setStartDate(OffsetDateTime.now(ZoneOffset.UTC));
        invalid.setEndDate(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1));

        mockMvc.perform(post("/api/clients/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST — client not found → 404")
    void createSubscription_clientNotFound_404() throws Exception {
        when(subscriptionService.createSubscription(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 99"));

        mockMvc.perform(post("/api/clients/99/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found with id: 99"));
    }

    @Test
    @DisplayName("POST — duplicate active subscription → 409 Conflict")
    void createSubscription_conflict_409() throws Exception {
        when(subscriptionService.createSubscription(eq(1L), any()))
                .thenThrow(new ConflictException("Client 1 already has an active MONTHLY subscription"));

        mockMvc.perform(post("/api/clients/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Client 1 already has an active MONTHLY subscription"));
    }

    @Test
    @DisplayName("POST — invalid dates (endDate = startDate) → 400")
    void createSubscription_invalidDates_400() throws Exception {
        when(subscriptionService.createSubscription(eq(1L), any()))
                .thenThrow(new IllegalArgumentException("endDate must be after startDate"));

        mockMvc.perform(post("/api/clients/1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endDate must be after startDate"));
    }

    // ── GET /api/clients/{clientId}/subscriptions ──────────────

    @Test
    @DisplayName("GET /api/clients/1/subscriptions — 200 with list")
    void getSubscriptionsByClient_ok() throws Exception {
        List<SubscriptionResponse> list = List.of(
                sampleResponse(1L, 1L),
                sampleResponse(2L, 1L));
        when(subscriptionService.getSubscriptionsByClient(1L)).thenReturn(list);

        mockMvc.perform(get("/api/clients/1/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET — client not found → 404")
    void getSubscriptionsByClient_notFound() throws Exception {
        when(subscriptionService.getSubscriptionsByClient(77L))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 77"));

        mockMvc.perform(get("/api/clients/77/subscriptions"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/clients/subscriptions/{id} ─────────────────

    @Test
    @DisplayName("DELETE /api/clients/subscriptions/5 — 204 No Content")
    void deactivateSubscription_noContent() throws Exception {
        doNothing().when(subscriptionService).deactivateSubscription(5L);

        mockMvc.perform(delete("/api/clients/subscriptions/5"))
                .andExpect(status().isNoContent());

        verify(subscriptionService).deactivateSubscription(5L);
    }

    @Test
    @DisplayName("DELETE — subscription not found → 404")
    void deactivateSubscription_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Subscription not found with id: 999"))
                .when(subscriptionService).deactivateSubscription(999L);

        mockMvc.perform(delete("/api/clients/subscriptions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Subscription not found with id: 999"));
    }
}

