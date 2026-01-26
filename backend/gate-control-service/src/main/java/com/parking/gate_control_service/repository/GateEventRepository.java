package com.parking.gate_control_service.repository;

import com.parking.gate_control_service.entity.GateEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for GateEvent entity.
 * Provides methods to query gate operation events.
 */
@Repository
public interface GateEventRepository extends JpaRepository<GateEvent, Long> {

    /**
     * Find all gate events for a specific license plate, ordered by timestamp descending.
     *
     * @param licensePlate the license plate to search for
     * @return list of gate events for the license plate, most recent first
     */
    List<GateEvent> findByLicensePlateOrderByTimestampDesc(String licensePlate);

    /**
     * Find all gate events within a time range.
     *
     * @param start the start of the time range (inclusive)
     * @param end   the end of the time range (inclusive)
     * @return list of gate events within the time range
     */
    List<GateEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
