package com.parking.api_gateway.security.dto;

import com.parking.api_gateway.security.entity.UserSecurityEntity;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    
    private UserSecurityEntity user;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime loginTime;
    private LocalDateTime sessionExpiryTime;
    private String ipAddress;
    private String userAgent;
    private boolean requiresPasswordChange;
    private boolean requiresTwoFactor;
    private Set<String> permissions;
    
    // Security metadata
    private int sessionTimeoutMinutes;
    private boolean isFirstLogin;
    private LocalDateTime lastLoginTime;
    private String lastLoginIpAddress;
}