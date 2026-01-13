package com.parking.client_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.client_service.exception.ConflictException;
import com.parking.client_service.exception.GlobalExceptionHandler;
import com.parking.client_service.generated.model.ClientRequest;
import com.parking.client_service.security.JwtTokenProvider;
import com.parking.client_service.service.ClientService;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ClientControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ClientService clientService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private ClientController clientController;

    @BeforeEach
    void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        this.mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void postClient_validationError_shouldReturn400() throws Exception {
        ClientRequest invalid = new ClientRequest(); // missing required fields

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postClient_conflict_shouldReturn409() throws Exception {
        ClientRequest req = new ClientRequest();
        req.setFullName("Test");
        req.setPhoneNumber("+100");

        doThrow(new ConflictException("Phone number already in use")).when(clientService).createClient(any(ClientRequest.class));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already in use"));
    }

    @Test
    void putClient_validationError_shouldReturn400() throws Exception {
        ClientRequest invalid = new ClientRequest();

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClient_notFound_shouldReturn404() throws Exception {
        when(clientService.findClientById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clients/999"))
                .andExpect(status().isNotFound());
    }
}
