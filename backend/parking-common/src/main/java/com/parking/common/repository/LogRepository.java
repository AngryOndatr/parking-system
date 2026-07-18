package com.parking.common.repository;

import com.parking.common.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Log entity.
 * Shared across all services via parking-common module.
 * Enables direct DB logging instead of HTTP calls to reporting-service.
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    /**
     * Find logs by service name.
     */
    List<Log> findByServiceOrderByTimestampDesc(String service);

    /**
     * Find logs by action type (e.g., GATE_ENTRY, CLIENT_CREATED).
     */
    List<Log> findByActionOrderByTimestampDesc(String action);

    /**
     * Find logs by client ID.
     */
    List<Log> findByClientIdOrderByTimestampDesc(Long clientId);

    /**
     * Find logs by license plate.
     */
    List<Log> findByLicensePlateOrderByTimestampDesc(String licensePlate);

    /**
     * Find logs in date range, ordered newest first.
     */
    @Query("SELECT l FROM Log l WHERE l.timestamp >= :from AND l.timestamp <= :to ORDER BY l.timestamp DESC")
    List<Log> findByTimestampRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Find audit logs (where action is not null) with optional filters.
     */
    @Query("SELECT l FROM Log l WHERE l.action IS NOT NULL " +
           "AND (:service IS NULL OR l.service = :service) " +
           "AND (:from IS NULL OR l.timestamp >= :from) " +
           "AND (:to IS NULL OR l.timestamp <= :to) " +
           "ORDER BY l.timestamp DESC")
    List<Log> findAuditLogs(
            @Param("service") String service,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Find audit logs for specific client.
     */
    @Query("SELECT l FROM Log l WHERE l.clientId = :clientId " +
           "AND (:from IS NULL OR l.timestamp >= :from) " +
           "AND (:to IS NULL OR l.timestamp <= :to) " +
           "ORDER BY l.timestamp DESC")
    List<Log> findClientHistory(
            @Param("clientId") Long clientId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /**
     * Find audit logs for specific vehicle by license plate.
     */
    @Query("SELECT l FROM Log l WHERE l.licensePlate = :licensePlate " +
           "AND (:from IS NULL OR l.timestamp >= :from) " +
           "AND (:to IS NULL OR l.timestamp <= :to) " +
           "ORDER BY l.timestamp DESC")
    List<Log> findVehicleHistory(
            @Param("licensePlate") String licensePlate,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
