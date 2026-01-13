package com.parking.reporting_service.controller;

import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.generated.controller.ReportingApi;
import com.parking.reporting_service.generated.model.LogRequest;
import com.parking.reporting_service.generated.model.LogResponse;
import com.parking.reporting_service.mapper.LogMapper;
import com.parking.reporting_service.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for reporting endpoints.
 * Implements the generated ReportingApi interface from OpenAPI spec.
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

            // Create log entry
            LogDomain createdLog = reportingService.createLog(logDomain);

            // Map domain back to response DTO
            LogResponse response = logMapper.toResponse(createdLog);

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
                                                         java.time.OffsetDateTime fromDate,
                                                         java.time.OffsetDateTime toDate,
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
}

