package com.parking.client_service.controller;
import com.parking.client_service.repository.SubscriptionRepository;
import com.parking.common.entity.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Unit tests for SubscriptionCheckController (Issue #72).
 * Controller implements the OpenAPI-generated SubscriptionApi interface.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionCheckController - subscription check endpoint")
class SubscriptionCheckControllerTest {
    private MockMvc mockMvc;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @InjectMocks
    private SubscriptionCheckController controller;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }
    @Test
    @DisplayName("GET /check?licensePlate=AA1234BB returns isAccessGranted=true when subscription found")
    void checkSubscription_activeSubscription_returnsAccessGranted() throws Exception {
        Subscription sub = new Subscription();
        sub.setIsActive(true);
        sub.setType("ANNUAL");
        sub.setStartDate(LocalDateTime.now().minusDays(1));
        sub.setEndDate(LocalDateTime.now().plusDays(364));
        when(subscriptionRepository.findActiveByLicensePlate(eq("AA1234BB"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(sub));
        mockMvc.perform(get("/api/v1/clients/subscriptions/check")
                        .param("licensePlate", "AA1234BB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAccessGranted").value(true))
                .andExpect(jsonPath("$.licensePlate").value("AA1234BB"))
                .andExpect(jsonPath("$.message").value("Active subscription found"));
    }
    @Test
    @DisplayName("GET /check?licensePlate=ZZ0000ZZ returns isAccessGranted=false when no subscription")
    void checkSubscription_noSubscription_returnsAccessDenied() throws Exception {
        when(subscriptionRepository.findActiveByLicensePlate(eq("ZZ0000ZZ"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/clients/subscriptions/check")
                        .param("licensePlate", "ZZ0000ZZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAccessGranted").value(false))
                .andExpect(jsonPath("$.licensePlate").value("ZZ0000ZZ"))
                .andExpect(jsonPath("$.message").value("No active subscription found"));
    }
}