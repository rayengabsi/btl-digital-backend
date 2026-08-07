package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.registration.CompleteRegistrationRequest;
import com.btl.digital.mock.dto.registration.CompleteRegistrationResponse;
import com.btl.digital.mock.dto.registration.SendEmailVerificationRequest;
import com.btl.digital.mock.dto.registration.VerifyClientRequest;
import com.btl.digital.mock.dto.registration.VerifyClientResponse;
import com.btl.digital.mock.dto.registration.VerifyEmailRequest;
import com.btl.digital.mock.dto.registration.VerifyPhoneOtpRequest;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.model.MockCustomer;
import com.btl.digital.mock.model.MockOtp;
import com.btl.digital.mock.model.MockRegistrationSession;
import com.btl.digital.mock.store.MockDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MockRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(MockRegistrationService.class);
    private static final String NEXT_STEP = "VERIFY_PHONE_OTP";
    private static final int PHONE_OTP_TTL = 5;
    private static final int EMAIL_CODE_TTL = 10;
    private static final int MAX_ATTEMPTS = 3;

    private final MockDataStore store;
    private final PasswordEncoder passwordEncoder;
    private final MockOtpProvider otpProvider;

    public MockRegistrationService(MockDataStore store, PasswordEncoder passwordEncoder, MockOtpProvider otpProvider) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
        this.otpProvider = otpProvider;
    }

    public VerifyClientResponse verifyClient(VerifyClientRequest request) {
        if (!"CIN".equals(request.getIdType()) && !"PASSPORT".equals(request.getIdType())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_ID_TYPE", "Unsupported ID type.");
        }

        MockCustomer customer = store.findCustomer(
                        request.getIdType(), request.getIdNumber(), request.getDateOfBirth(), request.getPhoneNumber())
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "T24_CUSTOMER_NOT_FOUND",
                        "T24 data not found or submitted phone does not match the registered phone."));

        if (customer.isRegistered()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "CLIENT_ALREADY_REGISTERED",
                    "T24 customer already has a digital account.");
        }

        MockOtp phoneOtp = otpProvider.generate(PHONE_OTP_TTL);

        String token = UUID.randomUUID().toString();
        MockRegistrationSession session = MockRegistrationSession.builder()
                .token(token)
                .t24CustomerId(customer.getCustomerId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .t24Verified(true)
                .phoneVerified(false)
                .emailVerified(false)
                .completed(false)
                .phoneOtp(phoneOtp)
                .build();
        store.addRegistrationSession(session);

        log.info("[MOCK PHONE OTP] to={} code={} ttl={}min",
                MockDataStore.normalizePhone(customer.getPhoneNumber()), phoneOtp.getCode(), PHONE_OTP_TTL);

        return VerifyClientResponse.builder()
                .registrationSessionToken(token)
                .nextStep(NEXT_STEP)
                .build();
    }

    public void verifyPhoneOtp(VerifyPhoneOtpRequest request) {
        MockRegistrationSession session = requireActiveSession(request.getRegistrationSessionToken());
        if (!session.isT24Verified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "T24_NOT_VERIFIED", "T24 verification missing.");
        }
        if (session.isPhoneVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "PHONE_ALREADY_VERIFIED", "Phone already verified.");
        }
        MockOtp otp = session.getPhoneOtp();
        verifyCode(session, otp, request.getOtp(), "OTP_NOT_FOUND");
        session.setPhoneVerified(true);
    }

    public void sendEmailVerification(SendEmailVerificationRequest request) {
        MockRegistrationSession session = requireActiveSession(request.getRegistrationSessionToken());
        if (!session.isPhoneVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "PHONE_NOT_VERIFIED", "Phone OTP must be verified first.");
        }
        if (session.isEmailVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_VERIFIED", "Email already verified.");
        }
        String email = request.getEmail().trim().toLowerCase();
        if (store.emailInUse(email)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_USED", "Email already used.");
        }
        if (session.getEmail() != null && !session.getEmail().equals(email)) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_VERIFIED", "A different email was already submitted.");
        }
        session.setEmail(email);
        MockOtp emailCode = otpProvider.generate(EMAIL_CODE_TTL);
        session.setEmailCode(emailCode);
        log.info("[MOCK EMAIL CODE] to={} code={} ttl={}min", email, emailCode.getCode(), EMAIL_CODE_TTL);
    }

    public void verifyEmail(VerifyEmailRequest request) {
        MockRegistrationSession session = requireActiveSession(request.getRegistrationSessionToken());
        if (!session.isPhoneVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "PHONE_NOT_VERIFIED", "Phone OTP must be verified first.");
        }
        if (session.getEmail() == null) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_NOT_SUBMITTED", "Email has not been submitted.");
        }
        if (session.isEmailVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_VERIFIED", "Email already verified.");
        }
        MockOtp code = session.getEmailCode();
        verifyCode(session, code, request.getCode(), "EMAIL_CODE_NOT_FOUND");
        session.setEmailVerified(true);
    }

    public CompleteRegistrationResponse complete(CompleteRegistrationRequest request) {
        MockRegistrationSession session = requireActiveSession(request.getRegistrationSessionToken());
        if (!session.isT24Verified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "T24_NOT_VERIFIED", "T24 verification missing.");
        }
        if (!session.isPhoneVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "PHONE_NOT_VERIFIED", "Phone not verified.");
        }
        if (!session.isEmailVerified()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "Email not verified.");
        }
        if (session.getEmail() == null) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email missing in session.");
        }
        if (session.getT24CustomerId() == null) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "T24_CUSTOMER_ID_REQUIRED", "T24 customer ID missing in session.");
        }
        if (store.emailInUse(session.getEmail())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_USED", "Email already used.");
        }

        MockClientUser user = MockClientUser.builder()
                .id(store.nextClientUserId())
                .email(session.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(session.getFirstName())
                .lastName(session.getLastName())
                .t24CustomerId(session.getT24CustomerId())
                .subscriptionId(1L)
                .status("ACTIVE")
                .firstLoginRequired(false)
                .phoneVerified(true)
                .emailVerified(true)
                .phoneNumber(store.customerByPhoneNullable(session.getT24CustomerId()))
                .emailAddress(session.getEmail())
                .build();
        store.addClientUser(user);

        session.setCompleted(true);
        store.markCustomerRegistered(session.getT24CustomerId(), user.getId());
        log.info("[MOCK] Registered client user id={} email={} linked to T24 customer {}",
                user.getId(), user.getEmail(), session.getT24CustomerId());

        return CompleteRegistrationResponse.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .accountStatus(user.getStatus())
                .firstLoginRequired(false)
                .build();
    }

    private MockRegistrationSession requireActiveSession(String token) {
        MockRegistrationSession session = store.registrationSessionByToken(token)
                .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_SESSION", "Registration session not found."));
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "SESSION_EXPIRED", "Registration session expired.");
        }
        if (session.isCompleted()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "REGISTRATION_ALREADY_COMPLETED", "Registration already completed.");
        }
        return session;
    }

    private void verifyCode(MockRegistrationSession session, MockOtp otp, String submitted, String notFoundCode) {
        if (otp == null) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, notFoundCode, "Verification code not found.");
        }
        if (otp.isUsed()) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_ALREADY_USED", "OTP already used.");
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "OTP expired.");
        }
        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "OTP_MAX_ATTEMPTS_EXCEEDED", "Too many OTP attempts.");
        }
        if (!otp.getCode().equals(submitted)) {
            otp.setAttempts(otp.getAttempts() + 1);
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "OTP value is wrong.");
        }
        otp.setUsed(true);
    }
}