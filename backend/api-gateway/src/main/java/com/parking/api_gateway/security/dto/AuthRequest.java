package com.parking.api_gateway.security.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 200, message = "Password must be between 8 and 200 characters")
    private String password;
    
    // Optional fields for enhanced security
    private String clientIpAddress;
    private String userAgent;
    private String deviceId;
    
    @Builder.Default
    private boolean rememberMe = false;
}