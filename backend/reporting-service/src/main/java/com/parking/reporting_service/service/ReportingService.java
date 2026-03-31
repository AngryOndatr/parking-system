package com.parking.reporting_service.service;

import com.parking.common.entity.Log;
import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for log management.
 * Contains business logic for persisting and retrieving system logs.
 */
@Service
public class ReportingService {

    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);
    private static final int DEFAULT_LIMIT = 100;

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

        Log entity = logDomain.getEntity();

        // ── Extract audit fields from meta ────────────────────────────
        Map<String, Object> meta = entity.getMeta();
        if (meta != null) {
            entity.setAction(asString(meta.get("action")));
            entity.setEntityType(asString(meta.get("entityType")));
            entity.setEntityId(asLong(meta.get("entityId")));
            entity.setClientId(asLong(meta.get("clientId")));
            entity.setLicensePlate(asString(meta.get("licensePlate")));
        }
        // ─────────────────────────────────────────────────────────────

        // Save entity
        Log saved = logRepository.save(entity);
        logger.info("✅ [REPORTING SERVICE] Log created id={}, action={}", saved.getId(), saved.getAction());
        return new LogDomain(saved);
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
                                               Instant fromDate, Instant toDate, Integer limit) {
        logger.info("🔍 [REPORTING SERVICE] Fetching logs with filters: level={}, service={}, userId={}, fromDate={}, toDate={}, limit={}",
                level, service, userId, fromDate, toDate, limit);

        // Start with all logs
        List<Log> entities = logRepository.findAll();

        // Apply filters
        var filtered = entities.stream()
                .filter(l -> level == null || level.equalsIgnoreCase(l.getLogLevel()))
                .filter(l -> service == null || service.equalsIgnoreCase(l.getService()))
                .filter(l -> userId == null || (l.getUserId() != null && l.getUserId().equals(userId)))
                .filter(l -> fromDate == null || !l.getTimestamp().toInstant(ZoneOffset.UTC).isBefore(fromDate))
                .filter(l -> toDate  == null || !l.getTimestamp().toInstant(ZoneOffset.UTC).isAfter(toDate))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())) // Newest first
                .limit(limit != null && limit > 0 ? limit : DEFAULT_LIMIT)
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

    // ── Audit trail methods ──────────────────────────────────────────

    /**
     * History of all business events for a given client.
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getClientHistory(Long clientId, LocalDateTime from, LocalDateTime to, int limit) {
        logger.info("🔍 [REPORTING SERVICE] Client history clientId={}, from={}, to={}, limit={}", clientId, from, to, limit);
        return logRepository.findClientHistory(clientId, from, to, limit, 0)
                .stream().map(LogDomain::new).collect(Collectors.toList());
    }

    /**
     * History of all business events for a given vehicle license plate.
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getVehicleHistory(String licensePlate, LocalDateTime from, LocalDateTime to, int limit) {
        logger.info("🔍 [REPORTING SERVICE] Vehicle history plate={}, from={}, to={}, limit={}", licensePlate, from, to, limit);
        return logRepository.findVehicleHistory(licensePlate.toUpperCase(), from, to, limit, 0)
                .stream().map(LogDomain::new).collect(Collectors.toList());
    }

    /**
     * History of all business events for a specific entity (type + id).
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getEntityHistory(String entityType, Long entityId, LocalDateTime from, LocalDateTime to, int limit) {
        return logRepository.findEntityHistory(entityType.toUpperCase(), entityId, from, to, limit, 0)
                .stream().map(LogDomain::new).collect(Collectors.toList());
    }

    /**
     * All audit events (have non-null action field), optionally filtered by service and date.
     */
    @Transactional(readOnly = true)
    public List<LogDomain> getAuditLogs(String service, LocalDateTime from, LocalDateTime to, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        var page = logRepository.findAuditLogs(service, from, to, pageable);
        return page.getContent().stream().map(LogDomain::new).collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private String asString(Object v) {
        return v instanceof String ? (String) v : null;
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Long)    return (Long) v;
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof Number)  return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }
}
