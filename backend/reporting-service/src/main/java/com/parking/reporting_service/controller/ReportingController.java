package com.parking.reporting_service.controller;

import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.generated.controller.ReportingApi;
import com.parking.reporting_service.generated.model.LogRequest;
import com.parking.reporting_service.generated.model.LogResponse;
import com.parking.reporting_service.mapper.LogMapper;
import com.parking.reporting_service.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for reporting endpoints.
 * Implements the generated ReportingApi interface from OpenAPI spec.
 * Additional audit-trail endpoints are added directly (not via OpenAPI generation).
 */
@RestController
public class ReportingController implements ReportingApi {

    private static final Logger logger = LoggerFactory.getLogger(ReportingController.class);

    private final ReportingService reportingService;
    private final LogMapper logMapper;

    public ReportingController(ReportingService reportingService, LogMapper logMapper) {
        this.reportingService = reportingService;
        this.logMapper = logMapper;
    }

    @Override
    public ResponseEntity<LogResponse> createLog(LogRequest logRequest) {
        logger.info("🚀 [REPORTING CONTROLLER] POST /api/reporting/log - level: {}, service: {}",
                logRequest.getLevel(), logRequest.getService());

        try {
            // Map DTO to domain
            LogDomain logDomain = logMapper.toDomain(logRequest);

            if (logDomain == null) {
                logger.error("❌ [REPORTING CONTROLLER] Mapper returned null domain");
                throw new IllegalArgumentException("Failed to map log request to domain");
            }

            // Create log entry
            LogDomain createdLog = reportingService.createLog(logDomain);

            if (createdLog == null) {
                logger.error("❌ [REPORTING CONTROLLER] Service returned null domain");
                throw new RuntimeException("Service failed to create log entry");
            }

            // Map domain back to response DTO
            LogResponse response = logMapper.toResponse(createdLog);

            if (response == null) {
                logger.error("❌ [REPORTING CONTROLLER] Mapper returned null response");
                throw new RuntimeException("Failed to map domain to response");
            }

            logger.info("✅ [REPORTING CONTROLLER] Log created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("❌ [REPORTING CONTROLLER] Invalid log request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ [REPORTING CONTROLLER] Error creating log: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create log entry", e);
        }
    }

    @Override
    public ResponseEntity<List<LogResponse>> getAllLogs(String level, String service, Long userId,
                                                         OffsetDateTime fromDate,
                                                         OffsetDateTime toDate,
                                                         Integer limit) {
        logger.info("🚀 [REPORTING CONTROLLER] GET /api/reporting/logs - filters: level={}, service={}, userId={}, fromDate={}, toDate={}, limit={}",
                level, service, userId, fromDate, toDate, limit);

        try {
            // Convert OffsetDateTime to Instant
            java.time.Instant fromInstant = fromDate != null ? fromDate.toInstant() : null;
            java.time.Instant toInstant = toDate != null ? toDate.toInstant() : null;

            List<LogDomain> logs = reportingService.getLogsWithFilters(level, service, userId, fromInstant, toInstant, limit);

            List<LogResponse> responses = logs.stream()
                    .map(logMapper::toResponse)
                    .collect(Collectors.toList());

            logger.info("✅ [REPORTING CONTROLLER] Retrieved {} filtered logs", responses.size());
            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            logger.error("❌ [REPORTING CONTROLLER] Error retrieving logs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve logs", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  AUDIT TRAIL ENDPOINTS  (not in OpenAPI spec — added directly)
    // ══════════════════════════════════════════════════════════════════

    /**
     * GET /api/reporting/audit
     * All business-event audit logs, newest-first.
     */
    @GetMapping("/api/reporting/audit")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "200") int limit) {

        try {
            List<LogDomain> logs = reportingService.getAuditLogs(service, from, to, limit);
            List<AuditLogResponse> responses = new ArrayList<>();
            for (LogDomain log : logs) {
                try {
                    responses.add(AuditLogResponse.from(log));
                } catch (Exception ex) {
                    logger.error("❌ [REPORTING CONTROLLER] Error mapping log {} to response: {}", log.getId(), ex.getMessage(), ex);
                    // Skip this log or handle as needed
                }
            }
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("❌ [REPORTING CONTROLLER] Error in getAuditLogs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve audit logs", e);
        }
    }

    /**
     * GET /api/reporting/audit/client/{clientId}
     * Full event history for a specific client.
     */
    @GetMapping("/api/reporting/audit/client/{clientId}")
    public ResponseEntity<List<AuditLogResponse>> getClientHistory(
            @PathVariable Long clientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "200") int limit) {

        try {
            List<LogDomain> logs = reportingService.getClientHistory(clientId, from, to, limit);
            List<AuditLogResponse> responses = new ArrayList<>();
            for (LogDomain log : logs) {
                try {
                    responses.add(AuditLogResponse.from(log));
                } catch (Exception ex) {
                    logger.error("❌ [REPORTING CONTROLLER] Error mapping log {} to response: {}", log.getId(), ex.getMessage(), ex);
                    // Skip this log or handle as needed
                }
            }
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("❌ [REPORTING CONTROLLER] Error in getClientHistory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve client history", e);
        }
    }

    /**
     * GET /api/reporting/audit/vehicle/{licensePlate}
     * Full event history for a specific vehicle (by license plate).
     */
    @GetMapping("/api/reporting/audit/vehicle/{licensePlate}")
    public ResponseEntity<List<AuditLogResponse>> getVehicleHistory(
            @PathVariable String licensePlate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "200") int limit) {

        try {
            List<LogDomain> logs = reportingService.getVehicleHistory(licensePlate, from, to, limit);
            List<AuditLogResponse> responses = new ArrayList<>();
            for (LogDomain log : logs) {
                try {
                    responses.add(AuditLogResponse.from(log));
                } catch (Exception ex) {
                    logger.error("❌ [REPORTING CONTROLLER] Error mapping log {} to response: {}", log.getId(), ex.getMessage(), ex);
                    // Skip this log or handle as needed
                }
            }
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("❌ [REPORTING CONTROLLER] Error in getVehicleHistory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve vehicle history", e);
        }
    }

    // ── Inline response DTO (avoids OpenAPI regeneration) ─────────────
    public record AuditLogResponse(
            Long   id,
            String timestamp,
            String level,
            String service,
            String action,
            String entityType,
            Long   entityId,
            Long   clientId,
            String licensePlate,
            String message,
            Object meta
    ) {
        static AuditLogResponse from(LogDomain d) {
            com.parking.common.entity.Log e = d.getEntity();
            return new AuditLogResponse(
                    e.getId(),
                    e.getTimestamp() != null ? e.getTimestamp().toString() : null,
                    e.getLogLevel(),
                    e.getService(),
                    e.getAction(),
                    e.getEntityType(),
                    e.getEntityId(),
                    e.getClientId(),
                    e.getLicensePlate(),
                    e.getMessage(),
                    e.getMeta()
            );
        }
    }
}
