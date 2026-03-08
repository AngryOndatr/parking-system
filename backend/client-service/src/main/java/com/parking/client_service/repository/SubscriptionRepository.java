package com.parking.client_service.repository;

import com.parking.common.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find an active, non-expired subscription for a vehicle identified by license plate.
     * Traverses: vehicles → clients → subscriptions.
     */
    @Query("""
            SELECT s FROM Subscription s
            JOIN s.client c
            JOIN Vehicle v ON v.client = c
            WHERE v.licensePlate = :licensePlate
              AND s.isActive = true
              AND s.endDate > :now
            """)
    Optional<Subscription> findActiveByLicensePlate(
            @Param("licensePlate") String licensePlate,
            @Param("now") LocalDateTime now);
}

