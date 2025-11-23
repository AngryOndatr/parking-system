package com.parking.client_service.controller;

import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.generated.model.ClientResponse;
import com.parking.client_service.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    // --- 1. CRUD:  ---
    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@RequestBody ClientRequest request) {
        ClientResponse response = clientService.createClient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- 2. CRUD:  ---
    @GetMapping
    public List<ClientResponse> getAllClients() {
        return clientService.findAllClients();
    }

    // --- 3. CRUD:  ---
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        return clientService.findClientById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- 4.  ---
    @GetMapping("/search")
    public ResponseEntity<ClientResponse> getClientByPhone(@RequestParam String phone) {
        return clientService.findClientByPhoneNumber(phone)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}