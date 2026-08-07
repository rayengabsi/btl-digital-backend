package com.btl.digital.mock.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompleteRegistrationRequest {
    @NotBlank(message = "Registration session token is required")
    private String registrationSessionToken;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%&*?])[A-Za-z\\d@#$%&*?]{10,}$",
            message = "Password must be at least 10 characters, contain an uppercase letter, a lowercase letter, a digit and one of @#$%&*?"
    )
    private String password;
}