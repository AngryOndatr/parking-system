package com.parking.reporting_service.service;

import com.parking.common.entity.Log;
import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for log management.
 * Contains business logic for persisting and retrieving system logs.
 */
@Service
public class ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);

    private final LogRepository logRepository;

    public ReportingService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Create and persist a new log entry.
     *
     * @param logDomain the log domain model to persist
     * @return the persisted log domain with generated ID
     */
    @Transactional
    public LogDomain createLog(LogDomain logDomain) {
        logger.info("📝 [REPORTING SERVICE] Creating log entry: level={}, service={}, message={}",
                logDomain.getLogLevel(), logDomain.getService(), logDomain.getMessage());

        // Validate domain object
        if (!logDomain.isValid()) {
            logger.error("❌ [REPORTING SERVICE] Invalid log domain: {}", logDomain);
            throw new IllegalArgumentException("Log entry must have timestamp, level, and message");
        }

        // Save entity
        Log savedEntity = logRepository.save(logDomain.getEntity());
        logger.info("✅ [REPORTING SERVICE] Log entry created with ID: {}", savedEntity.getId());

        return new LogDomain(savedEntity);
    }

    /**
     * Get all log entries.
     *
     * @return list of all log domains
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getAllLogs() {
        logger.info("📋 [REPORTING SERVICE] Fetching all log entries");

        List<Log> entities = logRepository.findAll();
        logger.info("✅ [REPORTING SERVICE] Found {} log entries", entities.size());

        return entities.stream()
                .map(LogDomain::new)
                .collect(Collectors.toList());
    }

    /**
     * Get logs with filters.
     *
     * @param level     filter by log level (optional)
     * @param service   filter by service name (optional)
     * @param userId    filter by user ID (optional)
     * @param fromDate  filter logs from this timestamp (optional)
     * @param toDate    filter logs until this timestamp (optional)
     * @param limit     maximum number of records (optional, default 100)
     * @return list of filtered log domains
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getLogsWithFilters(String level, String service, Long userId,
                                               java.time.Instant fromDate, java.time.Instant toDate,
                                               Integer limit) {
        logger.info("🔍 [REPORTING SERVICE] Fetching logs with filters: level={}, service={}, userId={}, fromDate={}, toDate={}, limit={}",
                level, service, userId, fromDate, toDate, limit);

        // Start with all logs
        List<Log> entities = logRepository.findAll();

        // Apply filters
        var filtered = entities.stream()
                .filter(log -> level == null || level.equalsIgnoreCase(log.getLogLevel()))
                .filter(log -> service == null || service.equalsIgnoreCase(log.getService()))
                .filter(log -> userId == null || (log.getUserId() != null && log.getUserId().equals(userId)))
                .filter(log -> fromDate == null || log.getTimestamp().toInstant(java.time.ZoneOffset.UTC).isAfter(fromDate) || log.getTimestamp().toInstant(java.time.ZoneOffset.UTC).equals(fromDate))
                .filter(log -> toDate == null || log.getTimestamp().toInstant(java.time.ZoneOffset.UTC).isBefore(toDate) || log.getTimestamp().toInstant(java.time.ZoneOffset.UTC).equals(toDate))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Newest first
                .limit(limit != null && limit > 0 ? limit : 100)
                .map(LogDomain::new)
                .collect(Collectors.toList());

        logger.info("✅ [REPORTING SERVICE] Found {} filtered log entries", filtered.size());

        return filtered;
    }

    /**
     * Get logs by service name.
     *
     * @param service the service name
     * @return list of log domains for the service
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getLogsByService(String service) {
        logger.info("🔍 [REPORTING SERVICE] Fetching logs for service: {}", service);

        List<Log> entities = logRepository.findByService(service);
        logger.info("✅ [REPORTING SERVICE] Found {} logs for service: {}", entities.size(), service);

        return entities.stream()
                .map(LogDomain::new)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by log level.
     *
     * @param logLevel the log level (INFO, WARN, ERROR, etc.)
     * @return list of log domains with the specified level
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getLogsByLevel(String logLevel) {
        logger.info("🔍 [REPORTING SERVICE] Fetching logs with level: {}", logLevel);

        List<Log> entities = logRepository.findByLogLevel(logLevel);
        logger.info("✅ [REPORTING SERVICE] Found {} logs with level: {}", entities.size(), logLevel);

        return entities.stream()
                .map(LogDomain::new)
                .collect(Collectors.toList());
    }
}

