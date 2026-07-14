package com.btl.digital.controller;

import com.btl.digital.dto.request.ForgotPasswordRequest;
import com.btl.digital.dto.request.LoginRequest;
import com.btl.digital.dto.request.RegisterRequest;
import com.btl.digital.dto.request.ResetPasswordRequest;
import com.btl.digital.dto.response.ApiMessageResponse;
import com.btl.digital.dto.response.LoginResponse;
import com.btl.digital.service.PasswordResetService;
import com.btl.digital.service.impl.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(ApiMessageResponse.builder()
                .message("If the email exists, a reset link has been sent.")
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiMessageResponse.builder()
                .message("Password has been reset successfully.")
                .build());
    }
}
