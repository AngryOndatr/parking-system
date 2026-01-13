package com.parking.client_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.GlobalExceptionHandler;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.VehicleCreateRequest;
import com.parking.client_service.generated.model.VehicleResponse;
import com.parking.client_service.generated.model.VehicleUpdateRequest;
import com.parking.client_service.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    private VehicleResponse testResponse;

    @BeforeEach
    void setUp() {
        testResponse = new VehicleResponse();
        testResponse.setId(1L);
        testResponse.setLicensePlate("AA1234BB");
        testResponse.setClientId(100L);
        testResponse.setIsAllowed(true);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        this.mockMvc = MockMvcBuilders.standaloneSetup(vehicleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getAllVehicles_ReturnsListOfVehicles() throws Exception {
        // Arrange
        VehicleResponse response2 = new VehicleResponse();
        response2.setId(2L);
        response2.setLicensePlate("BB5678CC");

        when(vehicleService.findAllVehicles()).thenReturn(Arrays.asList(testResponse, response2));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].licensePlate").value("AA1234BB"))
                .andExpect(jsonPath("$[1].licensePlate").value("BB5678CC"));
    }

    @Test
    void createVehicle_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setLicensePlate("AA1234BB");
        request.setClientId(100L);
        request.setIsAllowed(true);

        when(vehicleService.createVehicle(any(VehicleCreateRequest.class))).thenReturn(testResponse);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licensePlate").value("AA1234BB"))
                .andExpect(jsonPath("$.clientId").value(100));
    }

    @Test
    void createVehicle_InvalidRequest_MissingLicensePlate_ReturnsBadRequest() throws Exception {
        // Arrange
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setClientId(100L);
        // licensePlate is missing (required field)

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVehicle_LicensePlateAlreadyExists_ReturnsConflict() throws Exception {
        // Arrange
        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setLicensePlate("AA1234BB");
        request.setClientId(100L);

        when(vehicleService.createVehicle(any(VehicleCreateRequest.class)))
                .thenThrow(new ConflictException("License plate already in use"));

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getVehicleById_Found_ReturnsVehicle() throws Exception {
        // Arrange
        when(vehicleService.findVehicleById(1L)).thenReturn(Optional.of(testResponse));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.licensePlate").value("AA1234BB"));
    }

    @Test
    void getVehicleById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(vehicleService.findVehicleById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateVehicle_ValidRequest_ReturnsUpdated() throws Exception {
        // Arrange
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setLicensePlate("CC9999DD");
        request.setIsAllowed(false);

        VehicleResponse updatedResponse = new VehicleResponse();
        updatedResponse.setId(1L);
        updatedResponse.setLicensePlate("CC9999DD");
        updatedResponse.setIsAllowed(false);

        when(vehicleService.updateVehicle(eq(1L), any(VehicleUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("CC9999DD"))
                .andExpect(jsonPath("$.isAllowed").value(false));
    }

    @Test
    void updateVehicle_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setLicensePlate("CC9999DD");

        when(vehicleService.updateVehicle(eq(999L), any(VehicleUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Vehicle not found"));

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateVehicle_LicensePlateConflict_ReturnsConflict() throws Exception {
        // Arrange
        VehicleUpdateRequest request = new VehicleUpdateRequest();
        request.setLicensePlate("AA1234BB");

        when(vehicleService.updateVehicle(eq(1L), any(VehicleUpdateRequest.class)))
                .thenThrow(new ConflictException("License plate already in use"));

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteVehicle_Success_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(vehicleService).deleteVehicle(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(vehicleService).deleteVehicle(1L);
    }

    @Test
    void deleteVehicle_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Vehicle not found"))
                .when(vehicleService).deleteVehicle(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}

