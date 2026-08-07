package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A fake beneficiary owned by a client user.
 */
@Data
@Builder
public class MockBeneficiary {

    private Long id;
    private Long clientUserId;
    private String beneficiaryName;
    private String ribNumber;
    private LocalDateTime createdAt;
}