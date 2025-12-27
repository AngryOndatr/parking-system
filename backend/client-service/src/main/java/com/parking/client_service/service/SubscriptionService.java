package com.parking.client_service.service;

import com.parking.common.entity.Subscription;
import com.parking.client_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Long clientId) {
        log.info("Checking active subscription for client ID: {}", clientId);
        Optional<Subscription> subscription = subscriptionRepository
                .findActiveSubscriptionByClientId(clientId, LocalDateTime.now());
        
        boolean hasActive = subscription.isPresent();
        log.info("Client {} has active subscription: {}", clientId, hasActive);
        return hasActive;
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscription(Long clientId) {
        log.info("Getting active subscription for client ID: {}", clientId);
        return subscriptionRepository.findActiveSubscriptionByClientId(clientId, LocalDateTime.now());
    }
}
