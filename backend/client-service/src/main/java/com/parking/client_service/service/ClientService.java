package com.parking.client_service.service;

import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.mapper.ClientMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.common.entity.Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        // 1. 
        Client client = clientMapper.toEntity(request);
        
        // 2. 
        if (clientRepository.findByPhoneNumber(client.getPhoneNumber()).isPresent()) {
             throw new RuntimeException("Such Client's number already exists."); 
        }

        client.setRegisteredAt(LocalDateTime.now());

        // 3. 
        Client savedClient = clientRepository.save(client);
        return clientMapper.toResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> findAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ClientResponse> findClientById(Long id) {
        return clientRepository.findById(id)
                .map(clientMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ClientResponse> findClientByPhoneNumber(String phoneNumber) {
        return clientRepository.findByPhoneNumber(phoneNumber)
                .map(clientMapper::toResponse);
    }
}