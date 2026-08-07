package com.btl.digital.mock.controller;

import com.btl.digital.mock.common.ApiResponse;
import com.btl.digital.mock.dto.registration.CompleteRegistrationRequest;
import com.btl.digital.mock.dto.registration.CompleteRegistrationResponse;
import com.btl.digital.mock.dto.registration.SendEmailVerificationRequest;
import com.btl.digital.mock.dto.registration.VerifyClientRequest;
import com.btl.digital.mock.dto.registration.VerifyClientResponse;
import com.btl.digital.mock.dto.registration.VerifyEmailRequest;
import com.btl.digital.mock.dto.registration.VerifyPhoneOtpRequest;
import com.btl.digital.mock.service.MockRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registration")
public class MockRegistrationController {

    private final MockRegistrationService registrationService;

    public MockRegistrationController(MockRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/verify-client")
    public ResponseEntity<ApiResponse<VerifyClientResponse>> verifyClient(@Valid @RequestBody VerifyClientRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Client verified. OTP sent to phone number.", registrationService.verifyClient(request)));
    }

    @PostMapping("/verify-phone-otp")
    public ResponseEntity<ApiResponse<Void>> verifyPhoneOtp(@Valid @RequestBody VerifyPhoneOtpRequest request) {
        registrationService.verifyPhoneOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Phone number verified successfully."));
    }

    @PostMapping("/send-email-verification")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(@Valid @RequestBody SendEmailVerificationRequest request) {
        registrationService.sendEmailVerification(request);
        return ResponseEntity.ok(ApiResponse.success("Email verification code sent."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        registrationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully."));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<CompleteRegistrationResponse>> complete(@Valid @RequestBody CompleteRegistrationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Registration completed successfully.", registrationService.complete(request)));
    }
}