package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A single-use OTP issued for a registration step.
 */
@Data
@Builder
public class MockOtp {

    private String code;
    private LocalDateTime expiresAt;
    private boolean used;
    private int attempts;
}