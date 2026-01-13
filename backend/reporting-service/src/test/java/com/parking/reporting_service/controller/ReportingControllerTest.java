package com.parking.reporting_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.generated.model.LogRequest;
import com.parking.reporting_service.generated.model.LogResponse;
import com.parking.reporting_service.mapper.LogMapper;
import com.parking.reporting_service.service.ReportingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportingController.class)
@ActiveProfiles("test")
@DisplayName("ReportingController MockMvc Tests")
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private LogMapper logMapper;

    @Test
    @DisplayName("POST /api/reporting/log - Valid request returns 201 Created")
    void createLog_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        Map<String, Object> meta = new HashMap<>();
        meta.put("action", "CREATE");

        LogRequest request = new LogRequest();
        request.setTimestamp(OffsetDateTime.now());
        request.setLevel(LogRequest.LevelEnum.INFO);
        request.setService(JsonNullable.of("test-service"));
        request.setMessage("Test log message");
        request.setUserId(JsonNullable.of(1L));
        request.setMeta(JsonNullable.of(meta));

        LogDomain mockDomain = new LogDomain(
                LocalDateTime.now(),
                "INFO",
                "test-service",
                "Test log message",
                1L,
                meta
        );

        LogDomain savedDomain = new LogDomain(mockDomain.getEntity());
        savedDomain.getEntity().setId(1L);

        LogResponse expectedResponse = new LogResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTimestamp(OffsetDateTime.now());
        expectedResponse.setLevel("INFO");
        expectedResponse.setService(JsonNullable.of("test-service"));
        expectedResponse.setMessage("Test log message");
        expectedResponse.setUserId(JsonNullable.of(1L));
        expectedResponse.setMeta(JsonNullable.of(meta));

        when(logMapper.toDomain(any(LogRequest.class))).thenReturn(mockDomain);
        when(reportingService.createLog(any(LogDomain.class))).thenReturn(savedDomain);
        when(logMapper.toResponse(any(LogDomain.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/reporting/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.level").value("INFO"))
                .andExpect(jsonPath("$.service").value("test-service"))
                .andExpect(jsonPath("$.message").value("Test log message"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/reporting/log - Invalid request (missing required fields) returns 400")
    void createLog_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange - request without required fields
        String invalidJson = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/reporting/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/reporting/logs - Returns list of logs")
    void getAllLogs_ReturnsListOfLogs() throws Exception {
        // Arrange
        LogDomain domain1 = new LogDomain(
                LocalDateTime.now(),
                "INFO",
                "service1",
                "Message 1",
                null,
                null
        );
        domain1.getEntity().setId(1L);

        LogDomain domain2 = new LogDomain(
                LocalDateTime.now(),
                "ERROR",
                "service2",
                "Message 2",
                2L,
                null
        );
        domain2.getEntity().setId(2L);

        LogResponse response1 = new LogResponse();
        response1.setId(1L);
        response1.setLevel("INFO");
        response1.setService(JsonNullable.of("service1"));
        response1.setMessage("Message 1");

        LogResponse response2 = new LogResponse();
        response2.setId(2L);
        response2.setLevel("ERROR");
        response2.setService(JsonNullable.of("service2"));
        response2.setMessage("Message 2");

        when(reportingService.getAllLogs()).thenReturn(Arrays.asList(domain1, domain2));
        when(logMapper.toResponse(domain1)).thenReturn(response1);
        when(logMapper.toResponse(domain2)).thenReturn(response2);

        // Act & Assert
        mockMvc.perform(get("/api/reporting/logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].message").value("Message 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].message").value("Message 2"));
    }

    @Test
    @DisplayName("GET /api/reporting/logs - Returns empty array when no logs")
    void getAllLogs_NoLogs_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(reportingService.getAllLogs()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/reporting/logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

