package com.btl.digital.mock.service;

import com.btl.digital.mock.model.MockOtp;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Issues 6-digit codes and "sends" them to the console, mimicking SMS/email
 * providers without any real gateway.
 */
@Component
public class MockOtpProvider {

    private final SecureRandom random = new SecureRandom();

    public MockOtp generate(int ttlMinutes) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        return MockOtp.builder()
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .used(false)
                .attempts(0)
                .build();
    }

    public boolean isExpired(MockOtp otp) {
        return otp == null || otp.getExpiresAt().isBefore(LocalDateTime.now());
    }
}