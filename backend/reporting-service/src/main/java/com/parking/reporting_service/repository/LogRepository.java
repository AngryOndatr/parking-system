package com.parking.reporting_service.repository;

import com.parking.common.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Log entity.
 * Provides data access methods for system logs.
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    /**
     * Find logs by service name
     */
    List<Log> findByService(String service);

    /**
     * Find logs by log level
     */
    List<Log> findByLogLevel(String logLevel);

    /**
     * Find logs by timestamp range
     */
    List<Log> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find logs by user ID
     */
    List<Log> findByUserId(Long userId);

    /**
     * Find logs by service and level
     */
    List<Log> findByServiceAndLogLevel(String service, String logLevel);

    // ── Audit trail queries ──────────────────────────────────────────

    /** Full history for a client, newest-first, with optional date range */
    @Query(value = "SELECT * FROM logs l WHERE l.client_id = :clientId " +
           "AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR l.timestamp >= CAST(:fromDate AS TIMESTAMP)) " +
           "AND (CAST(:toDate AS TIMESTAMP) IS NULL OR l.timestamp <= CAST(:toDate AS TIMESTAMP)) " +
           "ORDER BY l.timestamp DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<Log> findClientHistory(
            @Param("clientId") Long clientId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /** Full history for a license plate, newest-first, with optional date range */
    @Query(value = "SELECT * FROM logs l WHERE l.license_plate = :plate " +
           "AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR l.timestamp >= CAST(:fromDate AS TIMESTAMP)) " +
           "AND (CAST(:toDate AS TIMESTAMP) IS NULL OR l.timestamp <= CAST(:toDate AS TIMESTAMP)) " +
           "ORDER BY l.timestamp DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<Log> findVehicleHistory(
            @Param("plate") String plate,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /** Generic entity history by type+id */
    @Query(value = "SELECT * FROM logs l WHERE l.entity_type = :entityType AND l.entity_id = :entityId " +
           "AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR l.timestamp >= CAST(:fromDate AS TIMESTAMP)) " +
           "AND (CAST(:toDate AS TIMESTAMP) IS NULL OR l.timestamp <= CAST(:toDate AS TIMESTAMP)) " +
           "ORDER BY l.timestamp DESC LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<Log> findEntityHistory(
            @Param("entityType") String entityType,
            @Param("entityId")   Long entityId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /** All audit events (action IS NOT NULL) with optional filters */
    @Query(value = "SELECT * FROM logs l WHERE l.action IS NOT NULL " +
           "AND (:service IS NULL OR l.service = :service) " +
           "AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR l.timestamp >= CAST(:fromDate AS TIMESTAMP)) " +
           "AND (CAST(:toDate AS TIMESTAMP) IS NULL OR l.timestamp <= CAST(:toDate AS TIMESTAMP)) " +
           "ORDER BY l.timestamp DESC",
           countQuery = "SELECT COUNT(*) FROM logs l WHERE l.action IS NOT NULL " +
           "AND (:service IS NULL OR l.service = :service) " +
           "AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR l.timestamp >= CAST(:fromDate AS TIMESTAMP)) " +
           "AND (CAST(:toDate AS TIMESTAMP) IS NULL OR l.timestamp <= CAST(:toDate AS TIMESTAMP))",
           nativeQuery = true)
    Page<Log> findAuditLogs(
            @Param("service") String service,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate")   LocalDateTime toDate,
            Pageable pageable);
}
