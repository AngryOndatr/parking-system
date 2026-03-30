package com.parking.client_service.repository;

import com.parking.common.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find an active, non-expired subscription for a vehicle identified by license plate.
     * Used by gate-control-service subscription check.
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

    // ── Subscription management queries ───────────────────────────

    /** All subscriptions for a client, newest first. */
    List<Subscription> findByClientIdOrderByStartDateDesc(Long clientId);

    /**
     * Returns true when the client already has an active subscription of the given type.
     * Used to enforce one-active-per-type constraint.
     */
    boolean existsByClientIdAndTypeAndIsActiveTrue(Long clientId, String type);
}


