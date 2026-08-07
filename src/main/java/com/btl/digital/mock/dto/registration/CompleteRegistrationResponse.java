package com.btl.digital.mock.dto.registration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompleteRegistrationResponse {
    private Long userId;
    private String username;
    private String accountStatus;
    private boolean firstLoginRequired;
}