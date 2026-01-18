package com.parking.billing.repository;

import com.parking.billing.entity.ParkingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingEvent entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ParkingEventRepository extends JpaRepository<ParkingEvent, Long> {

    /**
     * Find parking event by ticket code.
     *
     * @param ticketCode unique ticket code
     * @return Optional containing the parking event if found
     */
    Optional<ParkingEvent> findByTicketCode(String ticketCode);

    /**
     * Find all active parking events (no exit time) for a specific license plate.
     * Useful for checking if a vehicle is currently parked.
     *
     * @param licensePlate vehicle license plate
     * @return list of active parking events
     */
    List<ParkingEvent> findByLicensePlateAndExitTimeIsNull(String licensePlate);

    /**
     * Find parking events within a specific time range.
     * Useful for reporting and analytics.
     *
     * @param start start of time range (inclusive)
     * @param end   end of time range (inclusive)
     * @return list of parking events in the time range
     */
    List<ParkingEvent> findByEntryTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Check if a parking event with the given ticket code exists.
     *
     * @param ticketCode unique ticket code
     * @return true if exists, false otherwise
     */
    boolean existsByTicketCode(String ticketCode);
}

