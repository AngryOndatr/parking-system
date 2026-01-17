package com.parking.reporting_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.parking.reporting_service.domain.LogDomain;
import com.parking.reporting_service.exception.GlobalExceptionHandler;
import com.parking.reporting_service.generated.model.LogRequest;
import com.parking.reporting_service.generated.model.LogResponse;
import com.parking.reporting_service.mapper.LogMapper;
import com.parking.reporting_service.security.JwtTokenProvider;
import com.parking.reporting_service.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportingController MockMvc Tests")
class ReportingControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private ReportingService reportingService;

    @Mock
    private LogMapper logMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private ReportingController reportingController;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new JsonNullableModule());
        objectMapper.registerModule(new JsonNullableModule());

        this.mockMvc = MockMvcBuilders.standaloneSetup(reportingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("POST /api/reporting/log - Valid request returns 201 Created")
    void createLog_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        LogRequest request = new LogRequest();
        request.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        request.setLevel(LogRequest.LevelEnum.INFO);
        request.setService(JsonNullable.of("test-service"));
        request.setMessage("Test log message");
        request.setUserId(JsonNullable.of(1L));

        // Create mock domain objects
        LogDomain inputDomain = new LogDomain();
        inputDomain.setTimestamp(LocalDateTime.now());
        inputDomain.setLogLevel("INFO");
        inputDomain.setService("test-service");
        inputDomain.setMessage("Test log message");
        inputDomain.setUserId(1L);

        LogDomain savedDomain = new LogDomain();
        savedDomain.setTimestamp(LocalDateTime.now());
        savedDomain.setLogLevel("INFO");
        savedDomain.setService("test-service");
        savedDomain.setMessage("Test log message");
        savedDomain.setUserId(1L);

        LogResponse expectedResponse = new LogResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        expectedResponse.setLevel("INFO");
        expectedResponse.setService(JsonNullable.of("test-service"));
        expectedResponse.setMessage("Test log message");
        expectedResponse.setUserId(JsonNullable.of(1L));

        when(logMapper.toDomain(any(LogRequest.class))).thenReturn(inputDomain);
        when(reportingService.createLog(any(LogDomain.class))).thenReturn(savedDomain);
        when(logMapper.toResponse(any(LogDomain.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/reporting/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(result -> {
                    System.out.println("Response Status: " + result.getResponse().getStatus());
                    System.out.println("Response Body: " + result.getResponse().getContentAsString());
                    if (result.getResolvedException() != null) {
                        System.out.println("Exception: " + result.getResolvedException().getMessage());
                        result.getResolvedException().printStackTrace();
                    }
                })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.level").value("INFO"))
                .andExpect(jsonPath("$.service").value("test-service"))
                .andExpect(jsonPath("$.message").value("Test log message"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/reporting/log - Invalid request returns bad request")
    void createLog_InvalidRequest_ReturnsError() throws Exception {
        // Arrange - invalid request with missing required fields
        LogRequest invalidRequest = new LogRequest();
        // This should result in validation error (400 Bad Request)

        // Let the real error occur naturally - don't stub

        // Act & Assert
        mockMvc.perform(post("/api/reporting/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/reporting/logs - Returns list of logs")
    void getAllLogs_ReturnsListOfLogs() throws Exception {
        // Arrange
        LogDomain domain1 = new LogDomain(
                java.time.LocalDateTime.now(),
                "INFO",
                "service1",
                "Message 1",
                null,
                null
        );
        domain1.getEntity().setId(1L);

        LogDomain domain2 = new LogDomain(
                java.time.LocalDateTime.now(),
                "ERROR",
                "service2",
                "Message 2",
                2L,
                null
        );
        domain2.getEntity().setId(2L);

        LogResponse response1 = new LogResponse();
        response1.setId(1L);
        response1.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        response1.setLevel("INFO");
        response1.setService(JsonNullable.of("service1"));
        response1.setMessage("Message 1");

        LogResponse response2 = new LogResponse();
        response2.setId(2L);
        response2.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        response2.setLevel("ERROR");
        response2.setService(JsonNullable.of("service2"));
        response2.setMessage("Message 2");

        List<LogDomain> mockDomains = new ArrayList<>();
        mockDomains.add(domain1);
        mockDomains.add(domain2);

        when(reportingService.getLogsWithFilters(isNull(), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(mockDomains);
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
        when(reportingService.getLogsWithFilters(isNull(), isNull(), isNull(), isNull(), isNull(), anyInt()))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/reporting/logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

