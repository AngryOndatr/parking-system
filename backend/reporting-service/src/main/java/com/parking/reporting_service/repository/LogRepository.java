package com.parking.reporting_service.repository;

import com.parking.common.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

