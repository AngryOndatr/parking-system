package com.parking.billing_service.repository;

import com.parking.common.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Tariff entity operations.
 * Provides CRUD operations and custom queries for tariff management.
 */
@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    /**
     * Find an active tariff by tariff type.
     *
     * @param tariffType the type of tariff (e.g., "ONE_TIME", "DAILY", "NIGHT", "VIP")
     * @return Optional containing the active tariff if found, empty otherwise
     */
    Optional<Tariff> findByTariffTypeAndIsActiveTrue(String tariffType);

    /**
     * Check if a tariff with the given type already exists.
     *
     * @param tariffType the type of tariff to check
     * @return true if a tariff with this type exists, false otherwise
     */
    boolean existsByTariffType(String tariffType);
}

