package com.parking.reporting_service.service;

import com.parking.common.entity.Log;
import com.parking.reporting_service.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportingService {

    private final LogRepository logRepository;

    /**
     * Save a log entry
     */
    @Transactional
    public Log saveLog(Log logEntry) {
        log.info("Saving log entry: level={}, message={}", logEntry.getLogLevel(), logEntry.getMessage());
        
        if (logEntry.getTimestamp() == null) {
            logEntry.setTimestamp(LocalDateTime.now());
        }
        
        Log saved = logRepository.save(logEntry);
        log.debug("Log entry saved with ID: {}", saved.getId());
        return saved;
    }

    /**
     * Get logs by level
     */
    @Transactional(readOnly = true)
    public List<Log> getLogsByLevel(String level) {
        log.info("Fetching logs by level: {}", level);
        return logRepository.findByLogLevel(level);
    }

    /**
     * Get logs by user
     */
    @Transactional(readOnly = true)
    public List<Log> getLogsByUser(Long userId) {
        log.info("Fetching logs by user: {}", userId);
        return logRepository.findByUserId(userId);
    }

    /**
     * Get logs in time range
     */
    @Transactional(readOnly = true)
    public List<Log> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching logs between {} and {}", startTime, endTime);
        return logRepository.findByTimestampBetween(startTime, endTime);
    }

    /**
     * Get recent error logs
     */
    @Transactional(readOnly = true)
    public List<Log> getRecentErrorLogs(int hoursAgo) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hoursAgo);
        log.info("Fetching error logs from the last {} hours", hoursAgo);
        return logRepository.findByLogLevelAndTimestampAfter("ERROR", startTime);
    }

    /**
     * Get all logs
     */
    @Transactional(readOnly = true)
    public List<Log> getAllLogs() {
        log.info("Fetching all logs");
        return logRepository.findAll();
    }
}
