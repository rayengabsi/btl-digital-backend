package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A multi-factor login challenge built during the credential step.
 */
@Data
@Builder
public class MockLoginChallenge {

    private Long id;
    private Long clientUserId;
    private String challengeToken;
    private boolean mfaRequired;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String selectedChannel;
    private MockOtp otp;
    private int otpAttempts;
    private boolean consumed;
    private String maskedPhone;
    private String maskedEmail;
}