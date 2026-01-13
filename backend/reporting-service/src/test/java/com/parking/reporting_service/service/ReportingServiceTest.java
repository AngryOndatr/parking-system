package com.parking.reporting_service.service;

import com.parking.common.entity.Log;
import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportingService Unit Tests")
class ReportingServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private ReportingService reportingService;

    private LogDomain validLogDomain;
    private Log savedLog;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> meta = new HashMap<>();
        meta.put("action", "CREATE");
        meta.put("clientId", 123);

        validLogDomain = new LogDomain(now, "INFO", "client-service", "Test log message", 1L, meta);

        savedLog = new Log(now, "INFO", "client-service", "Test log message", 1L, meta);
        savedLog.setId(1L);
    }

    @Test
    @DisplayName("Should create log successfully")
    void createLog_ValidDomain_Success() {
        // Arrange
        when(logRepository.save(any(Log.class))).thenReturn(savedLog);

        // Act
        LogDomain result = reportingService.createLog(validLogDomain);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessage()).isEqualTo("Test log message");
        assertThat(result.getLogLevel()).isEqualTo("INFO");
        assertThat(result.getService()).isEqualTo("client-service");

        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logRepository, times(1)).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getMessage()).isEqualTo("Test log message");
    }

    @Test
    @DisplayName("Should throw exception when log domain is invalid - missing timestamp")
    void createLog_MissingTimestamp_ThrowsException() {
        // Arrange
        LogDomain invalidLog = new LogDomain();
        invalidLog.setLogLevel("INFO");
        invalidLog.setMessage("Test");

        // Act & Assert
        assertThatThrownBy(() -> reportingService.createLog(invalidLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("timestamp");

        verify(logRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when log domain is invalid - blank message")
    void createLog_BlankMessage_ThrowsException() {
        // Arrange
        LogDomain invalidLog = new LogDomain();
        invalidLog.setTimestamp(LocalDateTime.now());
        invalidLog.setLogLevel("INFO");
        invalidLog.setMessage("   ");

        // Act & Assert
        assertThatThrownBy(() -> reportingService.createLog(invalidLog))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("message");

        verify(logRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve all logs successfully")
    void getAllLogs_Success() {
        // Arrange
        Log log1 = new Log(LocalDateTime.now(), "INFO", "service1", "Message 1", null, null);
        log1.setId(1L);
        Log log2 = new Log(LocalDateTime.now(), "ERROR", "service2", "Message 2", 2L, null);
        log2.setId(2L);

        when(logRepository.findAll()).thenReturn(Arrays.asList(log1, log2));

        // Act
        List<LogDomain> results = reportingService.getAllLogs();

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(1).getId()).isEqualTo(2L);
        verify(logRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve logs by service")
    void getLogsByService_Success() {
        // Arrange
        String service = "client-service";
        Log log1 = new Log(LocalDateTime.now(), "INFO", service, "Message 1", null, null);
        log1.setId(1L);

        when(logRepository.findByService(service)).thenReturn(Arrays.asList(log1));

        // Act
        List<LogDomain> results = reportingService.getLogsByService(service);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getService()).isEqualTo(service);
        verify(logRepository, times(1)).findByService(service);
    }

    @Test
    @DisplayName("Should retrieve logs by level")
    void getLogsByLevel_Success() {
        // Arrange
        String level = "ERROR";
        Log log1 = new Log(LocalDateTime.now(), level, "service1", "Error message", null, null);
        log1.setId(1L);

        when(logRepository.findByLogLevel(level)).thenReturn(Arrays.asList(log1));

        // Act
        List<LogDomain> results = reportingService.getLogsByLevel(level);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLogLevel()).isEqualTo(level);
        verify(logRepository, times(1)).findByLogLevel(level);
    }
}

