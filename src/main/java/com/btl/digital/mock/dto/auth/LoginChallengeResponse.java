package com.btl.digital.mock.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginChallengeResponse {
    private Long challengeId;
    private String challengeToken;
    private boolean mfaRequired;
    private List<ChannelOption> availableChannels;
    private String expiresAt;

    @Data
    @Builder
    public static class ChannelOption {
        private String channel;
        private String maskedDestination;
    }
}