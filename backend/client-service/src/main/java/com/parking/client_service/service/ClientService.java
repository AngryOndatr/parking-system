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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional
    public ClientResponse createClient(ClientRequestDto requestDto) {
        log.info("Creating new client with phone: {}", requestDto.getPhoneNumber());

        // Map DTO -> Entity
        Client client = clientMapper.toEntity(requestDto);

        // uniqueness checks
        if (clientRepository.findByPhoneNumber(client.getPhoneNumber()).isPresent()) {
            log.warn("Failed to create client - phone number already in use: {}", client.getPhoneNumber());
            throw new ConflictException("Phone number already in use");
        }
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            log.warn("Failed to create client - email already in use: {}", client.getEmail());
            throw new ConflictException("Email already in use");
        }

        client.setRegisteredAt(LocalDateTime.now());

        Client savedClient = clientRepository.save(client);
        log.info("Successfully created client with id: {}, phone: {}", savedClient.getId(), savedClient.getPhoneNumber());

        // Map to response while still in transaction (defensive, prevents lazy issues)
        return clientMapper.toResponse(savedClient);
    }

    // Preserve compatibility for generated controller paths that use ClientRequest
    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        log.info("Creating new client (via generated API) with phone: {}", request.getPhoneNumber());

        Client client = clientMapper.toEntity(request);

        if (clientRepository.findByPhoneNumber(client.getPhoneNumber()).isPresent()) {
            log.warn("Failed to create client - phone number already in use: {}", client.getPhoneNumber());
            throw new ConflictException("Phone number already in use");
        }
        if (client.getEmail() != null && clientRepository.findByEmail(client.getEmail()).isPresent()) {
            log.warn("Failed to create client - email already in use: {}", client.getEmail());
            throw new ConflictException("Email already in use");
        }

        client.setRegisteredAt(LocalDateTime.now());

        Client savedClient = clientRepository.save(client);
        log.info("Successfully created client with id: {}, phone: {}", savedClient.getId(), savedClient.getPhoneNumber());

        return clientMapper.toResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> findAllClients() {
        log.debug("Retrieving all clients");
        // map inside transaction to avoid lazy-loading outside tx
        List<ClientResponse> clients = clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
        log.debug("Found {} clients", clients.size());
        return clients;
    }

    @Transactional(readOnly = true)
    public Optional<ClientResponse> findClientById(Long id) {
        log.debug("Searching for client with id: {}", id);
        Optional<ClientResponse> result = clientRepository.findById(id)
                .map(clientMapper::toResponse);
        if (result.isPresent()) {
            log.debug("Client found with id: {}", id);
        } else {
            log.debug("Client not found with id: {}", id);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<ClientResponse> findClientByPhoneNumber(String phoneNumber) {
        log.debug("Searching for client with phone number: {}", phoneNumber);
        Optional<ClientResponse> result = clientRepository.findByPhoneNumber(phoneNumber)
                .map(clientMapper::toResponse);
        if (result.isPresent()) {
            log.debug("Client found with phone number: {}", phoneNumber);
        } else {
            log.debug("Client not found with phone number: {}", phoneNumber);
        }
        return result;
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientRequestDto requestDto) {
        log.info("Updating client with id: {}", id);

        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to update - client not found with id: {}", id);
                    return new ResourceNotFoundException("Client not found with id: " + id);
                });

        // wrap entity in domain to operate in business layer
        ClientDomain domain = new ClientDomain(existing);

        // check phone uniqueness (if changing)
        if (!ObjectsEqual(domain.getPhoneNumber(), requestDto.getPhoneNumber())) {
            log.debug("Phone number change detected for client id: {}", id);
            clientRepository.findByPhoneNumber(requestDto.getPhoneNumber()).ifPresent(c -> {
                log.warn("Failed to update client {} - phone number already in use: {}", id, requestDto.getPhoneNumber());
                throw new ConflictException("Phone number already in use");
            });
        }

        // check email uniqueness (if changing and not null)
        if (requestDto.getEmail() != null && !ObjectsEqual(requestDto.getEmail(), domain.getEmail())) {
            log.debug("Email change detected for client id: {}", id);
            clientRepository.findByEmail(requestDto.getEmail()).ifPresent(c -> {
                log.warn("Failed to update client {} - email already in use: {}", id, requestDto.getEmail());
                throw new ConflictException("Email already in use");
            });
        }

        // apply changes via domain (delegates to entity)
        domain.setFullName(requestDto.getFullName());
        domain.setPhoneNumber(requestDto.getPhoneNumber());
        domain.setEmail(requestDto.getEmail());

        Client saved = clientRepository.save(domain.getEntity());
        log.info("Successfully updated client with id: {}, phone: {}", saved.getId(), saved.getPhoneNumber());

        // return response while in transaction
        return clientMapper.toResponse(saved);
    }

    @Transactional
    public void deleteClient(Long id) {
        log.info("Deleting client with id: {}", id);

        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to delete - client not found with id: {}", id);
                    return new ResourceNotFoundException("Client not found with id: " + id);
                });

        clientRepository.delete(existing);
        log.info("Successfully deleted client with id: {}", id);
    }

    // New overload: update using generated ClientRequest
    @Transactional
    public ClientResponse updateClient(Long id, ClientRequest request) {
        log.info("Updating client (via generated API) with id: {}", id);
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
