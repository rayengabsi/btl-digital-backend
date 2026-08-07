package com.btl.digital.mock.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendEmailVerificationRequest {
    @NotBlank(message = "Registration session token is required")
    private String registrationSessionToken;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}