package com.parking.gate_control_service.service;

import com.parking.gate_control_service.client.ClientServiceClient;
import com.parking.gate_control_service.dto.EntryDecision;
import com.parking.gate_control_service.dto.SubscriptionCheckResponse;
import com.parking.gate_control_service.entity.GateEvent;
import com.parking.gate_control_service.repository.GateEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GateService.
 * Tests entry decision logic for subscribers and one-time visitors.
 */
@ExtendWith(MockitoExtension.class)
class GateServiceTest {

    @Mock
    private ClientServiceClient clientServiceClient;

    @Mock
    private GateEventRepository gateEventRepository;

    @InjectMocks
    private GateService gateService;

    @Test
    void processEntry_Subscriber_GrantsAccessWithoutTicket() {
        // Given: Vehicle with active subscription
        String licensePlate = "ABC123";
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(true)
                .clientId(100L)
                .subscriptionId(200L)
                .build();

        when(clientServiceClient.checkSubscription(licensePlate)).thenReturn(subscriptionResponse);
        when(gateEventRepository.save(any(GateEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Process entry
        EntryDecision decision = gateService.processEntry(licensePlate);

        // Then: Access granted without ticket
        assertThat(decision).isNotNull();
        assertThat(decision.getAction()).isEqualTo("OPEN");
        assertThat(decision.getMessage()).isEqualTo("Welcome, subscriber!");
        assertThat(decision.getTicketCode()).isNull(); // No ticket for subscribers

        // Verify gate event saved
        ArgumentCaptor<GateEvent> eventCaptor = ArgumentCaptor.forClass(GateEvent.class);
        verify(gateEventRepository, times(1)).save(eventCaptor.capture());
        GateEvent savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getEventType()).isEqualTo(GateEvent.EventType.ENTRY);
        assertThat(savedEvent.getLicensePlate()).isEqualTo(licensePlate);
        assertThat(savedEvent.getDecision()).isEqualTo(GateEvent.Decision.OPEN);
        assertThat(savedEvent.getReason()).contains("Valid subscription");
        assertThat(savedEvent.getReason()).contains("200");
        assertThat(savedEvent.getGateId()).isEqualTo("ENTRY-1");
        assertThat(savedEvent.getTicketCode()).isNull();
    }

    @Test
    void processEntry_OneTimeVisitor_GeneratesTicketAndGrantsAccess() {
        // Given: Vehicle without subscription (one-time visitor)
        String licensePlate = "XYZ789";
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        when(clientServiceClient.checkSubscription(licensePlate)).thenReturn(subscriptionResponse);
        when(gateEventRepository.save(any(GateEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Process entry
        EntryDecision decision = gateService.processEntry(licensePlate);

        // Then: Access granted with ticket
        assertThat(decision).isNotNull();
        assertThat(decision.getAction()).isEqualTo("OPEN");
        assertThat(decision.getMessage()).isEqualTo("Take your ticket");
        assertThat(decision.getTicketCode()).isNotNull();
        assertThat(decision.getTicketCode()).startsWith("TICKET-");
        assertThat(decision.getTicketCode()).contains("-"); // Should have timestamp and random part

        // Verify gate event saved
        ArgumentCaptor<GateEvent> eventCaptor = ArgumentCaptor.forClass(GateEvent.class);
        verify(gateEventRepository, times(1)).save(eventCaptor.capture());
        GateEvent savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getEventType()).isEqualTo(GateEvent.EventType.ENTRY);
        assertThat(savedEvent.getLicensePlate()).isEqualTo(licensePlate);
        assertThat(savedEvent.getDecision()).isEqualTo(GateEvent.Decision.OPEN);
        assertThat(savedEvent.getReason()).isEqualTo("Ticket issued");
        assertThat(savedEvent.getGateId()).isEqualTo("ENTRY-1");
        assertThat(savedEvent.getTicketCode()).isNotNull();
        assertThat(savedEvent.getTicketCode()).startsWith("TICKET-");
    }

    @Test
    void processEntry_SubscriberPath_ClientServiceCalledOnce() {
        // Given
        String licensePlate = "SUB001";
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(true)
                .clientId(1L)
                .subscriptionId(1L)
                .build();

        when(clientServiceClient.checkSubscription(licensePlate)).thenReturn(subscriptionResponse);
        when(gateEventRepository.save(any(GateEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gateService.processEntry(licensePlate);

        // Then: Client service called exactly once
        verify(clientServiceClient, times(1)).checkSubscription(licensePlate);
    }

    @Test
    void processEntry_VisitorPath_ClientServiceCalledOnce() {
        // Given
        String licensePlate = "VIS001";
        SubscriptionCheckResponse subscriptionResponse = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        when(clientServiceClient.checkSubscription(licensePlate)).thenReturn(subscriptionResponse);
        when(gateEventRepository.save(any(GateEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gateService.processEntry(licensePlate);

        // Then: Client service called exactly once
        verify(clientServiceClient, times(1)).checkSubscription(licensePlate);
    }

    @Test
    void processEntry_MultipleVisitors_GenerateUniqueTickets() {
        // Given: Multiple visitors
        String licensePlate1 = "VIS001";
        String licensePlate2 = "VIS002";
        SubscriptionCheckResponse noSubscription = SubscriptionCheckResponse.builder()
                .isAccessGranted(false)
                .build();

        when(clientServiceClient.checkSubscription(anyString())).thenReturn(noSubscription);
        when(gateEventRepository.save(any(GateEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Process multiple entries
        EntryDecision decision1 = gateService.processEntry(licensePlate1);
        EntryDecision decision2 = gateService.processEntry(licensePlate2);

        // Then: Tickets are unique
        assertThat(decision1.getTicketCode()).isNotNull();
        assertThat(decision2.getTicketCode()).isNotNull();
        assertThat(decision1.getTicketCode()).isNotEqualTo(decision2.getTicketCode());
    }
}
