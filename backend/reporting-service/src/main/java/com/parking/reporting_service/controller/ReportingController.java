package com.parking.reporting_service.controller;

import com.parking.common.entity.Log;
import com.parking.reporting_service.service.ReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reporting")
@Slf4j
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;

    /**
     * POST /api/reporting/log
     * Internal API for receiving and storing logs
     */
    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> createLog(@RequestBody Log logEntry) {
        log.info("🚀 [REPORTING CONTROLLER] POST /api/reporting/log - level: {}", logEntry.getLogLevel());
        
        try {
            Log saved = reportingService.saveLog(logEntry);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("logId", saved.getId());
            response.put("timestamp", saved.getTimestamp());
            
            log.info("✅ [REPORTING CONTROLLER] Log saved with ID: {}", saved.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("❌ [REPORTING CONTROLLER] Error saving log: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * GET /api/reporting/logs
     * Retrieve logs with optional filters
     */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("🚀 [REPORTING CONTROLLER] GET /api/reporting/logs - level: {}, userId: {}", level, userId);
        
        List<Log> logs;
        
        if (level != null) {
            logs = reportingService.getLogsByLevel(level);
        } else if (userId != null) {
            logs = reportingService.getLogsByUser(userId);
        } else if (startTime != null && endTime != null) {
            logs = reportingService.getLogsByTimeRange(startTime, endTime);
        } else {
            logs = reportingService.getAllLogs();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", logs.size());
        response.put("logs", logs);
        
        log.info("✅ [REPORTING CONTROLLER] Returning {} logs", logs.size());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reporting/logs/errors
     * Get recent error logs
     */
    @GetMapping("/logs/errors")
    public ResponseEntity<Map<String, Object>> getRecentErrors(
            @RequestParam(defaultValue = "24") int hours) {
        
        log.info("🚀 [REPORTING CONTROLLER] GET /api/reporting/logs/errors - hours: {}", hours);
        
        List<Log> errorLogs = reportingService.getRecentErrorLogs(hours);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", errorLogs.size());
        response.put("hoursAgo", hours);
        response.put("logs", errorLogs);
        
        log.info("✅ [REPORTING CONTROLLER] Returning {} error logs", errorLogs.size());
        return ResponseEntity.ok(response);
    }
}
