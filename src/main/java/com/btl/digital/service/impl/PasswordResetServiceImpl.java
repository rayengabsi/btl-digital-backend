package com.btl.digital.service.impl;

import com.btl.digital.dto.request.ForgotPasswordRequest;
import com.btl.digital.dto.request.ResetPasswordRequest;
import com.btl.digital.entity.PasswordResetToken;
import com.btl.digital.entity.User;
import com.btl.digital.exception.ExpiredTokenException;
import com.btl.digital.exception.InvalidTokenException;
import com.btl.digital.repository.PasswordResetTokenRepository;
import com.btl.digital.repository.UserRepository;
import com.btl.digital.service.EmailService;
import com.btl.digital.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.security.reset-token-expiration}")
    private int resetTokenExpirationMinutes;

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes))
                    .used(false)
                    .user(user)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user, resetToken.getToken(), resetTokenExpirationMinutes);

            log.info("Password reset token generated for user: {}", user.getEmail());
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("Reset token has expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());
    }
}
