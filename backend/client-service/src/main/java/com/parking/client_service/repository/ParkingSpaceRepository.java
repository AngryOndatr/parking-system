package com.parking.client_service.repository;

import com.parking.common.entity.ParkingSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for ParkingSpace.
 * Used by SubscriptionService to reserve / release parking spaces
 * when subscriptions are created or deactivated.
 */
@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
}

