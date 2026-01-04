package com.parking.client_service.service;

import com.parking.client_service.dto.VehicleRequestDto;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.generated.model.VehicleResponse;
import com.parking.client_service.mapper.VehicleMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.client_service.repository.VehicleRepository;
import com.parking.common.entity.Client;
import com.parking.common.entity.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    private Client testClient;
    private Vehicle testVehicle;
    private VehicleRequestDto testRequestDto;
    private VehicleResponse testResponse;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setFullName("Test Client");
        testClient.setPhoneNumber("+123456789");

        testVehicle = new Vehicle();
        testVehicle.setId(1L);
        testVehicle.setLicensePlate("AA1234BB");
        testVehicle.setClient(testClient);
        testVehicle.setIsAllowed(true);

        testRequestDto = new VehicleRequestDto("AA1234BB", 1L, true);

        testResponse = new VehicleResponse();
        testResponse.setId(1L);
        testResponse.setLicensePlate("AA1234BB");
        testResponse.setClientId(1L);
        testResponse.setIsAllowed(true);
    }

    @Test
    void createVehicle_Success() {
        // Arrange
        when(vehicleRepository.findByLicensePlate(testRequestDto.licensePlate())).thenReturn(Optional.empty());
        when(clientRepository.findById(testRequestDto.clientId())).thenReturn(Optional.of(testClient));
        when(vehicleMapper.toEntity(testRequestDto)).thenReturn(testVehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        when(vehicleMapper.toResponse(testVehicle)).thenReturn(testResponse);

        // Act
        VehicleResponse result = vehicleService.createVehicle(testRequestDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getLicensePlate()).isEqualTo("AA1234BB");
        assertThat(result.getClientId()).isEqualTo(1L);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_LicensePlateAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(vehicleRepository.findByLicensePlate(testRequestDto.licensePlate()))
                .thenReturn(Optional.of(testVehicle));

        // Act & Assert
        assertThatThrownBy(() -> vehicleService.createVehicle(testRequestDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("License plate already in use");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void createVehicle_ClientNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(vehicleRepository.findByLicensePlate(testRequestDto.licensePlate())).thenReturn(Optional.empty());
        when(clientRepository.findById(testRequestDto.clientId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vehicleService.createVehicle(testRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void findAllVehicles_ReturnsListOfVehicles() {
        // Arrange
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId(2L);
        vehicle2.setLicensePlate("BB5678CC");
        vehicle2.setClient(testClient);

        VehicleResponse response2 = new VehicleResponse();
        response2.setId(2L);
        response2.setLicensePlate("BB5678CC");

        when(vehicleRepository.findAll()).thenReturn(Arrays.asList(testVehicle, vehicle2));
        when(vehicleMapper.toResponse(testVehicle)).thenReturn(testResponse);
        when(vehicleMapper.toResponse(vehicle2)).thenReturn(response2);

        // Act
        List<VehicleResponse> result = vehicleService.findAllVehicles();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLicensePlate()).isEqualTo("AA1234BB");
        assertThat(result.get(1).getLicensePlate()).isEqualTo("BB5678CC");
    }

    @Test
    void findVehicleById_Found_ReturnsVehicle() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleMapper.toResponse(testVehicle)).thenReturn(testResponse);

        // Act
        Optional<VehicleResponse> result = vehicleService.findVehicleById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getLicensePlate()).isEqualTo("AA1234BB");
    }

    @Test
    void findVehicleById_NotFound_ReturnsEmpty() {
        // Arrange
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<VehicleResponse> result = vehicleService.findVehicleById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void deleteVehicle_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));

        // Act
        vehicleService.deleteVehicle(1L);

        // Assert
        verify(vehicleRepository).delete(testVehicle);
    }

    @Test
    void deleteVehicle_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vehicleService.deleteVehicle(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).delete(any());
    }
}

