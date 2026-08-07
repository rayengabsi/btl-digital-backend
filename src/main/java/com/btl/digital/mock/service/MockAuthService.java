package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.auth.AuthSuccessResponse;
import com.btl.digital.mock.dto.auth.ChooseMfaRequest;
import com.btl.digital.mock.dto.auth.ChooseMfaResponse;
import com.btl.digital.mock.dto.auth.LoginChallengeResponse;
import com.btl.digital.mock.dto.auth.LoginRequest;
import com.btl.digital.mock.dto.auth.VerifyOtpRequest;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.model.MockLoginChallenge;
import com.btl.digital.mock.model.MockOtp;
import com.btl.digital.mock.store.MockDataStore;
import com.btl.digital.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MockAuthService {

    private static final Logger log = LoggerFactory.getLogger(MockAuthService.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final long TOKEN_TTL_SECONDS = 900;

    private final MockDataStore store;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MockOtpProvider otpProvider;

    public MockAuthService(MockDataStore store, PasswordEncoder passwordEncoder,
                           JwtService jwtService, MockOtpProvider otpProvider) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpProvider = otpProvider;
    }

    public LoginChallengeResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        MockClientUser user = store.clientByEmail(email)
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "BAD_CREDENTIALS", "Email or password is wrong."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleBadPassword(user);
        }
        resetFailedAttempts(user);

        if (isLocked(user)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_LOCKED", "Account is temporarily locked.");
        }
        if ("SUSPENDED".equals(user.getStatus()) || "CLOSED".equals(user.getStatus())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "ACCOUNT_NOT_ALLOWED", "Account is suspended or closed.");
        }

        MockLoginChallenge challenge = MockLoginChallenge.builder()
                .id(store.nextLoginChallengeId())
                .clientUserId(user.getId())
                .challengeToken(UUID.randomUUID().toString())
                .mfaRequired(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .maskedPhone(maskPhone(user.getPhoneNumber()))
                .maskedEmail(maskEmail(user.getEmailAddress()))
                .otpAttempts(0)
                .consumed(false)
                .build();
        store.addLoginChallenge(challenge);

        List<LoginChallengeResponse.ChannelOption> channels = new ArrayList<>();
        if (user.isPhoneVerified() && user.getPhoneNumber() != null) {
            channels.add(LoginChallengeResponse.ChannelOption.builder()
                    .channel("PHONE").maskedDestination(challenge.getMaskedPhone()).build());
        }
        if (user.isEmailVerified() && user.getEmailAddress() != null) {
            channels.add(LoginChallengeResponse.ChannelOption.builder()
                    .channel("EMAIL").maskedDestination(challenge.getMaskedEmail()).build());
        }
        if (channels.isEmpty()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "MFA_DESTINATION_REQUIRED",
                    "User has no verified phone or email destination.");
        }

        return LoginChallengeResponse.builder()
                .challengeId(challenge.getId())
                .challengeToken(challenge.getChallengeToken())
                .mfaRequired(true)
                .availableChannels(channels)
                .expiresAt(challenge.getExpiresAt().format(ISO))
                .build();
    }

    public ChooseMfaResponse chooseMfa(ChooseMfaRequest request) {
        MockLoginChallenge challenge = getChallenge(request.getChallengeId(), request.getChallengeToken());

        if (challenge.getSelectedChannel() != null) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "MFA_CHANNEL_ALREADY_SELECTED",
                    "A channel was already selected for this challenge.");
        }

        MockClientUser user = store.clientById(challenge.getClientUserId())
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "AUTHENTICATION_REQUIRED", "User not found."));

        String masked;
        if ("PHONE".equals(request.getMfaChannel())) {
            if (!user.isPhoneVerified()) {
                throw new MockApiException(HttpStatus.BAD_REQUEST, "VERIFIED_PHONE_REQUIRED", "Verified phone number required.");
            }
            masked = challenge.getMaskedPhone();
        } else if ("EMAIL".equals(request.getMfaChannel())) {
            if (!user.isEmailVerified()) {
                throw new MockApiException(HttpStatus.BAD_REQUEST, "VERIFIED_EMAIL_REQUIRED", "Verified email address required.");
            }
            masked = challenge.getMaskedEmail();
        } else {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid MFA channel.");
        }

        MockOtp otp = otpProvider.generate(5);
        challenge.setOtp(otp);
        challenge.setSelectedChannel(request.getMfaChannel());
        log.info("[MOCK LOGIN OTP] channel={} destination={} code={}", request.getMfaChannel(), masked, otp.getCode());

        return ChooseMfaResponse.builder()
                .challengeId(challenge.getId())
                .channel(request.getMfaChannel())
                .maskedDestination(masked)
                .expiresAt(otp.getExpiresAt().format(ISO))
                .build();
    }

    public AuthSuccessResponse verifyOtp(VerifyOtpRequest request) {
        MockLoginChallenge challenge = getChallenge(request.getChallengeId(), request.getChallengeToken());

        if (challenge.getOtp() == null) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "MFA_CHANNEL_NOT_SELECTED",
                    "OTP verification attempted before selecting a channel.");
        }

        if (challenge.isConsumed()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "LOGIN_CHALLENGE_EXPIRED",
                    "Credential challenge has expired or was already consumed.");
        }

        MockOtp otp = challenge.getOtp();
        if (otp.isUsed()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_ALREADY_USED", "Challenge OTP has already been consumed.");
        }
        if (otpProvider.isExpired(otp)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "Challenge OTP has expired.");
        }
        if (challenge.getOtpAttempts() >= 3) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_MAX_ATTEMPTS_EXCEEDED",
                    "Three incorrect OTP attempts have been reached.");
        }
        if (!otp.getCode().equals(request.getOtp())) {
            challenge.setOtpAttempts(challenge.getOtpAttempts() + 1);
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Submitted OTP is incorrect.");
        }

        otp.setUsed(true);
        challenge.setConsumed(true);

        MockClientUser user = store.clientById(challenge.getClientUserId())
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "AUTHENTICATION_REQUIRED", "User not found."));

        String token = jwtService.generateToken(toUserDetails(user));

        return AuthSuccessResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInSeconds(TOKEN_TTL_SECONDS)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accountStatus(user.getStatus())
                .build();
    }

    private MockLoginChallenge getChallenge(Long id, String token) {
        MockLoginChallenge challenge = store.loginChallenge(id)
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "LOGIN_CHALLENGE_NOT_FOUND",
                        "Challenge ID/token combination is invalid."));
        if (!challenge.getChallengeToken().equals(token)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "LOGIN_CHALLENGE_NOT_FOUND",
                    "Challenge ID/token combination is invalid.");
        }
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now()) || challenge.isConsumed()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "LOGIN_CHALLENGE_EXPIRED",
                    "Credential challenge has expired or was already consumed.");
        }
        return challenge;
    }

    private void handleBadPassword(MockClientUser user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= 5) {
            user.setStatus("LOCKED");
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            log.warn("[MOCK] Account {} locked for 15 minutes after 5 failed attempts", user.getEmail());
        }
        throw new MockApiException(HttpStatus.BAD_REQUEST, "BAD_CREDENTIALS", "Email or password is wrong.");
    }

    private void resetFailedAttempts(MockClientUser user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
        }
        if ("LOCKED".equals(user.getStatus()) && user.getLockedUntil() != null
                && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setStatus("ACTIVE");
            user.setLockedUntil(null);
        }
    }

    private boolean isLocked(MockClientUser user) {
        return "LOCKED".equals(user.getStatus())
                && (user.getLockedUntil() == null || user.getLockedUntil().isAfter(LocalDateTime.now()));
    }

    private User toUserDetails(MockClientUser user) {
        return new User(user.getEmail(), user.getPasswordHash(), List.of());
    }

    private String maskPhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        return "****" + (digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits);
    }

    private String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "****" + email.substring(at - 1);
    }
}