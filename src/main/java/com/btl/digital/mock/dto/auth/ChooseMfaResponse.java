package com.btl.digital.mock.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChooseMfaResponse {
    private Long challengeId;
    private String channel;
    private String maskedDestination;
    private String expiresAt;
}