package com.parking.client_service.service;

import com.parking.client_service.dto.ClientRequestDto;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.mapper.ClientMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.common.entity.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_success() {
        ClientRequestDto dto = new ClientRequestDto("John Doe", "+123456789", "john@example.com");

        Client entity = new Client();
        entity.setFullName(dto.getFullName());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());

        Client saved = new Client();
        saved.setId(1L);
        saved.setFullName(dto.getFullName());
        saved.setPhoneNumber(dto.getPhoneNumber());
        saved.setEmail(dto.getEmail());
        saved.setRegisteredAt(LocalDateTime.now());

        com.parking.client_service.generated.model.ClientResponse response = new com.parking.client_service.generated.model.ClientResponse();
        response.setId(1L);
        response.setFullName(dto.getFullName());
        response.setPhoneNumber(dto.getPhoneNumber());
        response.setEmail(dto.getEmail());

        when(clientMapper.toEntity(dto)).thenReturn(entity);
        when(clientRepository.findByPhoneNumber(dto.getPhoneNumber())).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(saved);
        when(clientMapper.toResponse(saved)).thenReturn(response);

        ClientResponse result = clientService.createClient(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository, times(1)).save(captor.capture());
        Client captured = captor.getValue();
        assertThat(captured.getPhoneNumber()).isEqualTo(dto.getPhoneNumber());
    }

    @Test
    void findById_success() {
        Client entity = new Client();
        entity.setId(2L);
        entity.setFullName("Jane");
        entity.setPhoneNumber("+987654321");

        com.parking.client_service.generated.model.ClientResponse response = new com.parking.client_service.generated.model.ClientResponse();
        response.setId(2L);
        response.setFullName("Jane");
        response.setPhoneNumber("+987654321");

        when(clientRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(clientMapper.toResponse(entity)).thenReturn(response);

        Optional<com.parking.client_service.generated.model.ClientResponse> maybe = clientService.findClientById(2L);

        assertThat(maybe).isPresent();
        assertThat(maybe.get().getId()).isEqualTo(2L);
    }
}

