package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A fake bank account from the mock "T24" backend.
 */
@Data
@Builder
public class MockAccount {

    private String accountId;
    private String arrangementId;
    private String productName;
    private String iban;
    private String currencyId;
    private BigDecimal availableBalance;
    private BigDecimal workingBalance;
    private Long ownerClientUserId;
}