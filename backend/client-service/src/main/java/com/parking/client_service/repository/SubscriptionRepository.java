package com.parking.client_service.repository;

import com.parking.common.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find an active, non-expired subscription for a vehicle identified by license plate.
     * Uses native SQL to avoid Hibernate 6 entity-join / lazy-association quirks.
     * Path: vehicles.license_plate → vehicles.client_id → subscriptions.client_id
     */
    @Query(value = """
            SELECT s.* FROM subscriptions s
            WHERE s.is_active = true
              AND s.end_date > NOW()
              AND s.client_id = (
                  SELECT v.client_id FROM vehicles v
                  WHERE v.license_plate = :licensePlate
                  LIMIT 1
              )
            LIMIT 1
            """, nativeQuery = true)
    Optional<Subscription> findActiveByLicensePlate(
            @Param("licensePlate") String licensePlate);
}
