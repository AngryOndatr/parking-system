package com.parking.client_service.service;

import com.parking.client_service.dto.ClientRequestDto;
import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.ResourceNotFoundException;
import com.parking.client_service.mapper.ClientMapper;
import com.parking.client_service.repository.ClientRepository;
import com.parking.common.domain.ClientDomain;
import com.parking.common.entity.Client;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    public ClientResponse createClient(ClientRequestDto requestDto) {
        // Map DTO -> Entity
        Client client = clientMapper.toEntity(requestDto);

        // uniqueness checks
        if (clientRepository.findByPhoneNumber(client.getPhoneNumber()).isPresent()) {
            throw new ConflictException("Phone number already in use");
        }
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new ConflictException("Email already in use");
        }

        client.setRegisteredAt(LocalDateTime.now());

        Client savedClient = clientRepository.save(client);

        // Wrap in domain for business usage (inside transaction)
        ClientDomain domain = new ClientDomain(savedClient);
        // potential place for domain-level business logic

        // Map to response while still in transaction (defensive, prevents lazy issues)
        return clientMapper.toResponse(savedClient);
    }

    // Preserve compatibility for generated controller paths that use ClientRequest
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        Client client = clientMapper.toEntity(request);

        if (clientRepository.findByPhoneNumber(client.getPhoneNumber()).isPresent()) {
            throw new ConflictException("Phone number already in use");
        }
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new ConflictException("Email already in use");
        }

        client.setRegisteredAt(LocalDateTime.now());

        Client savedClient = clientRepository.save(client);

        ClientDomain domain = new ClientDomain(savedClient);

        return clientMapper.toResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> findAllClients() {
        // map inside transaction to avoid lazy-loading outside tx
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

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequestDto requestDto) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // wrap entity in domain to operate in business layer
        ClientDomain domain = new ClientDomain(existing);

        // check phone uniqueness (if changing)
        if (!ObjectsEqual(domain.getPhoneNumber(), requestDto.getPhoneNumber())) {
            clientRepository.findByPhoneNumber(requestDto.getPhoneNumber()).ifPresent(c -> {
                throw new ConflictException("Phone number already in use");
            });
        }

        // check email uniqueness (if changing and not null)
        if (requestDto.getEmail() != null && !ObjectsEqual(requestDto.getEmail(), domain.getEmail())) {
            clientRepository.findByEmail(requestDto.getEmail()).ifPresent(c -> {
                throw new ConflictException("Email already in use");
            });
        }

        // apply changes via domain (delegates to entity)
        domain.setFullName(requestDto.getFullName());
        domain.setPhoneNumber(requestDto.getPhoneNumber());
        domain.setEmail(requestDto.getEmail());

        Client saved = clientRepository.save(domain.getEntity());

        // return response while in transaction
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        clientRepository.delete(existing);
    }

    // New overload: update using generated ClientRequest
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        // Map generated request to DTO-like values and reuse logic
        ClientRequestDto dto = new ClientRequestDto(request.getFullName(), request.getPhoneNumber(), request.getEmail());
        return updateClient(id, dto);
    }

    // small helper for null-safe equality
    private boolean ObjectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
