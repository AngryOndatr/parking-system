package com.parking.client_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDto {

    @NotBlank(message = "fullName is required")
    @Size(max = 255, message = "fullName must be at most 255 characters")
    private String fullName;

    @NotBlank(message = "phoneNumber is required")
    @Size(max = 50, message = "phoneNumber must be at most 50 characters")
    private String phoneNumber;

    @Email(message = "email must be a valid email address")
    @Size(max = 255, message = "email must be at most 255 characters")
    private String email;
}

