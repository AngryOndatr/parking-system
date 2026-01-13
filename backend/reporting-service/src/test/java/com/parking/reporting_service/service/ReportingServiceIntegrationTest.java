package com.parking.reporting_service.service;

import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.repository.LogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportingService Integration Tests")
class ReportingServiceIntegrationTest {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private LogRepository logRepository;

    @Test
    @DisplayName("Should persist log to database successfully")
    void createLog_PersistsToDatabase() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> meta = new HashMap<>();
        meta.put("action", "TEST");
        meta.put("value", 42);

        LogDomain logDomain = new LogDomain(now, "INFO", "test-service", "Integration test log", 1L, meta);

        // Act
        LogDomain savedLog = reportingService.createLog(logDomain);

        // Assert
        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getMessage()).isEqualTo("Integration test log");
        assertThat(savedLog.getService()).isEqualTo("test-service");
        assertThat(savedLog.getLogLevel()).isEqualTo("INFO");
        assertThat(savedLog.getUserId()).isEqualTo(1L);
        assertThat(savedLog.getMeta()).containsEntry("action", "TEST");
        assertThat(savedLog.getMeta()).containsEntry("value", 42);

        // Verify it's actually in the database
        assertThat(logRepository.findById(savedLog.getId())).isPresent();
    }

    @Test
    @DisplayName("Should retrieve all logs from database")
    void getAllLogs_RetrievesFromDatabase() {
        // Arrange - create multiple logs
        LogDomain log1 = new LogDomain(LocalDateTime.now(), "INFO", "service1", "Log 1", null, null);
        LogDomain log2 = new LogDomain(LocalDateTime.now(), "WARN", "service2", "Log 2", 2L, null);
        LogDomain log3 = new LogDomain(LocalDateTime.now(), "ERROR", "service3", "Log 3", null, null);

        reportingService.createLog(log1);
        reportingService.createLog(log2);
        reportingService.createLog(log3);

        // Act
        List<LogDomain> allLogs = reportingService.getAllLogs();

        // Assert
        assertThat(allLogs).hasSizeGreaterThanOrEqualTo(3);
        assertThat(allLogs).anyMatch(log -> "Log 1".equals(log.getMessage()));
        assertThat(allLogs).anyMatch(log -> "Log 2".equals(log.getMessage()));
        assertThat(allLogs).anyMatch(log -> "Log 3".equals(log.getMessage()));
    }

    @Test
    @DisplayName("Should filter logs by service name")
    void getLogsByService_FiltersCorrectly() {
        // Arrange
        String targetService = "my-service";
        LogDomain log1 = new LogDomain(LocalDateTime.now(), "INFO", targetService, "Target log", null, null);
        LogDomain log2 = new LogDomain(LocalDateTime.now(), "INFO", "other-service", "Other log", null, null);

        reportingService.createLog(log1);
        reportingService.createLog(log2);

        // Act
        List<LogDomain> serviceLogs = reportingService.getLogsByService(targetService);

        // Assert
        assertThat(serviceLogs).isNotEmpty();
        assertThat(serviceLogs).allMatch(log -> targetService.equals(log.getService()));
    }

    @Test
    @DisplayName("Should filter logs by level")
    void getLogsByLevel_FiltersCorrectly() {
        // Arrange
        LogDomain errorLog = new LogDomain(LocalDateTime.now(), "ERROR", "service1", "Error occurred", null, null);
        LogDomain infoLog = new LogDomain(LocalDateTime.now(), "INFO", "service2", "Info message", null, null);

        reportingService.createLog(errorLog);
        reportingService.createLog(infoLog);

        // Act
        List<LogDomain> errorLogs = reportingService.getLogsByLevel("ERROR");

        // Assert
        assertThat(errorLogs).isNotEmpty();
        assertThat(errorLogs).allMatch(log -> "ERROR".equals(log.getLogLevel()));
    }

    @Test
    @DisplayName("Should handle log with null userId and meta")
    void createLog_WithNullOptionalFields_Success() {
        // Arrange
        LogDomain logDomain = new LogDomain(LocalDateTime.now(), "DEBUG", null, "Simple log", null, null);

        // Act
        LogDomain savedLog = reportingService.createLog(logDomain);

        // Assert
        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getUserId()).isNull();
        assertThat(savedLog.getService()).isNull();
        assertThat(savedLog.getMeta()).isNull();
    }
}

