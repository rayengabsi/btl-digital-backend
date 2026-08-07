package com.btl.digital.mock.controller;

import com.btl.digital.mock.common.ApiResponse;
import com.btl.digital.mock.dto.auth.AuthSuccessResponse;
import com.btl.digital.mock.dto.auth.ChooseMfaRequest;
import com.btl.digital.mock.dto.auth.ChooseMfaResponse;
import com.btl.digital.mock.dto.auth.LoginChallengeResponse;
import com.btl.digital.mock.dto.auth.LoginRequest;
import com.btl.digital.mock.dto.auth.VerifyOtpRequest;
import com.btl.digital.mock.service.MockAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class MockAuthController {

    private final MockAuthService authService;

    public MockAuthController(MockAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginChallengeResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login credentials verified.", authService.login(request)));
    }

    @PostMapping("/login/choose-mfa")
    public ResponseEntity<ApiResponse<ChooseMfaResponse>> chooseMfa(@Valid @RequestBody ChooseMfaRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login OTP sent.", authService.chooseMfa(request)));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse<AuthSuccessResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful.", authService.verifyOtp(request)));
    }
}