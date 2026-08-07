package com.btl.digital.mock.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChooseMfaRequest {
    @NotNull(message = "Challenge ID is required")
    private Long challengeId;

    @NotBlank(message = "Challenge token is required")
    private String challengeToken;

    @NotBlank(message = "MFA channel is required")
    @Pattern(regexp = "PHONE|EMAIL", message = "MFA channel must be PHONE or EMAIL")
    private String mfaChannel;
}