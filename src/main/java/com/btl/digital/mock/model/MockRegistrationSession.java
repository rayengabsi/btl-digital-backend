package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Tracks a multi-step registration session.
 */
@Data
@Builder
public class MockRegistrationSession {

    private String token;
    private Long clientUserId;
    private String t24CustomerId;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean t24Verified;
    private boolean phoneVerified;
    private boolean emailVerified;
    private String email;
    private boolean completed;

    private MockOtp phoneOtp;
    private MockOtp emailCode;
}