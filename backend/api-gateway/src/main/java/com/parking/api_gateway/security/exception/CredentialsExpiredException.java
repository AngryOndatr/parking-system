package com.parking.api_gateway.security.exception;

import org.springframework.security.core.AuthenticationException;

public class CredentialsExpiredException extends AuthenticationException {
    
    public CredentialsExpiredException(String message) {
        super(message);
    }
    
    public CredentialsExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}