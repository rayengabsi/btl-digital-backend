package com.btl.digital.mock.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank(message = "Registration session token is required")
    private String registrationSessionToken;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "\\d{6}", message = "Code must contain exactly 6 digits")
    private String code;
}