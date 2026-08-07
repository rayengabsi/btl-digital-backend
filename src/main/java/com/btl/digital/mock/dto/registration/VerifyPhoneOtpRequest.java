package com.btl.digital.mock.dto.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPhoneOtpRequest {
    @NotBlank(message = "Registration session token is required")
    private String registrationSessionToken;

    @NotBlank(message = "OTP is required")
    private String otp;
}