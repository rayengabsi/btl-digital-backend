package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A registered digital banking user (CLIENT_USER). Backed by a T24 customer id.
 */
@Data
@Builder
public class MockClientUser {

    private Long id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String t24CustomerId;
    private String phoneNumber;
    private String emailAddress;
    private Long subscriptionId;
    /** ACTIVE, SUSPENDED, CLOSED, LOCKED */
    private String status;
    private boolean firstLoginRequired;
    private LocalDateTime lockedUntil;
    private int failedLoginAttempts;
    private boolean phoneVerified;
    private boolean emailVerified;
}