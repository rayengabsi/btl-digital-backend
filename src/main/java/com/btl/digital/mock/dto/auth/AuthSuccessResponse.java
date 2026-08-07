package com.btl.digital.mock.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthSuccessResponse {
    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String accountStatus;
}