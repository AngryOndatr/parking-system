package com.parking.client_service.service;

import com.parking.client_service.audit.AuditLogger;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.SubscriptionRequest;
import com.parking.client_service.generated.model.SubscriptionResponse;
import com.parking.client_service.mapper.SubscriptionMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.client_service.repository.ParkingSpaceRepository;
import com.parking.client_service.repository.SubscriptionRepository;
import com.parking.common.entity.Client;
import com.parking.common.entity.Subscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService unit tests")
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private ClientRepository       clientRepository;
    @Mock private ParkingSpaceRepository parkingSpaceRepository;
    @Mock private SubscriptionMapper     subscriptionMapper;
    @Mock private AuditLogger            auditLogger;

    @InjectMocks
    private SubscriptionService subscriptionService;

    // ── helpers ───────────────────────────────────────────────────

    private Client makeClient(Long id) {
        Client c = new Client();
        c.setId(id);
        c.setFullName("Test Client");
        c.setPhoneNumber("+380501234567");
        return c;
    }

    private Subscription makeSubscription(Long id, Client client, String type) {
        Subscription s = new Subscription();
        s.setId(id);
        s.setClient(client);
        s.setType(type);
        s.setStartDate(LocalDateTime.now());
        s.setEndDate(LocalDateTime.now().plusMonths(1));
        s.setIsActive(true);
        return s;
    }

    private SubscriptionRequest makeRequest(SubscriptionRequest.TypeEnum type) {
        SubscriptionRequest req = new SubscriptionRequest();
        req.setType(type);
        req.setStartDate(OffsetDateTime.now(ZoneOffset.UTC));
        req.setEndDate(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1));
        return req;
    }

    private SubscriptionResponse makeResponse(Long id, Long clientId, String type) {
        SubscriptionResponse r = new SubscriptionResponse();
        r.setId(id);
        r.setClientId(clientId);
        r.setType(type);
        r.setIsActive(true);
        return r;
    }

    // ── createSubscription ────────────────────────────────────────

    @Test
    @DisplayName("createSubscription — happy path → 201 with correct clientId")
    void createSubscription_success() {
        Client client = makeClient(1L);
        SubscriptionRequest req = makeRequest(SubscriptionRequest.TypeEnum.MONTHLY);
        Subscription entity  = makeSubscription(null, client, "MONTHLY");
        Subscription saved   = makeSubscription(10L,  client, "MONTHLY");
        SubscriptionResponse expected = makeResponse(10L, 1L, "MONTHLY");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(subscriptionRepository.existsByClientIdAndTypeAndIsActiveTrue(1L, "MONTHLY")).thenReturn(false);
        when(subscriptionMapper.toEntity(req, client)).thenReturn(entity);
        when(subscriptionMapper.toDomain(entity)).thenReturn(new com.parking.common.domain.SubscriptionDomain(entity));
        when(subscriptionRepository.save(entity)).thenReturn(saved);
        when(subscriptionMapper.toResponse(saved)).thenReturn(expected);

        SubscriptionResponse result = subscriptionService.createSubscription(1L, req);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getClientId()).isEqualTo(1L);
        verify(subscriptionRepository).save(entity);
    }

    @Test
    @DisplayName("createSubscription — client not found → ResourceNotFoundException")
    void createSubscription_clientNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        SubscriptionRequest req = makeRequest(SubscriptionRequest.TypeEnum.MONTHLY);
        assertThatThrownBy(() -> subscriptionService.createSubscription(99L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("createSubscription — endDate <= startDate → IllegalArgumentException")
    void createSubscription_invalidDates() {
        Client client = makeClient(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        SubscriptionRequest req = new SubscriptionRequest();
        req.setType(SubscriptionRequest.TypeEnum.MONTHLY);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        req.setStartDate(now);
        req.setEndDate(now); // same — not after

        assertThatThrownBy(() -> subscriptionService.createSubscription(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endDate");
    }

    @Test
    @DisplayName("createSubscription — duplicate active type → ConflictException")
    void createSubscription_duplicateActiveType() {
        Client client = makeClient(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(subscriptionRepository.existsByClientIdAndTypeAndIsActiveTrue(1L, "ANNUAL")).thenReturn(true);

        SubscriptionRequest req = makeRequest(SubscriptionRequest.TypeEnum.ANNUAL);
        assertThatThrownBy(() -> subscriptionService.createSubscription(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ANNUAL");
    }

    // ── getSubscriptionsByClient ──────────────────────────────────

    @Test
    @DisplayName("getSubscriptionsByClient — returns mapped list")
    void getSubscriptionsByClient_success() {
        Client client = makeClient(2L);
        Subscription s1 = makeSubscription(1L, client, "MONTHLY");
        Subscription s2 = makeSubscription(2L, client, "ANNUAL");

        when(clientRepository.existsById(2L)).thenReturn(true);
        when(subscriptionRepository.findByClientIdOrderByStartDateDesc(2L))
                .thenReturn(List.of(s1, s2));
        when(subscriptionMapper.toResponse(s1)).thenReturn(makeResponse(1L, 2L, "MONTHLY"));
        when(subscriptionMapper.toResponse(s2)).thenReturn(makeResponse(2L, 2L, "ANNUAL"));

        List<SubscriptionResponse> result = subscriptionService.getSubscriptionsByClient(2L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SubscriptionResponse::getId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("getSubscriptionsByClient — client not found → ResourceNotFoundException")
    void getSubscriptionsByClient_clientNotFound() {
        when(clientRepository.existsById(55L)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.getSubscriptionsByClient(55L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("55");
    }

    // ── deactivateSubscription ────────────────────────────────────

    @Test
    @DisplayName("deactivateSubscription — sets isActive=false and saves")
    void deactivateSubscription_success() {
        Client client = makeClient(1L);
        Subscription entity = makeSubscription(7L, client, "MONTHLY");

        when(subscriptionRepository.findById(7L)).thenReturn(Optional.of(entity));
        when(subscriptionMapper.toDomain(entity))
                .thenReturn(new com.parking.common.domain.SubscriptionDomain(entity));
        when(subscriptionRepository.save(any())).thenReturn(entity);

        subscriptionService.deactivateSubscription(7L);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("deactivateSubscription — not found → ResourceNotFoundException")
    void deactivateSubscription_notFound() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.deactivateSubscription(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}

