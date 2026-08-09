package com.btl.digital.mock.dto.dev;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Response for the development deposit, returning the updated balances of
 * the primary mock account.
 */
@Data
@Builder
public class DevAccountDepositResponse {
    private String accountId;
    private String iban;
    private String currencyId;
    private BigDecimal amount;
    private BigDecimal availableBalance;
    private BigDecimal workingBalance;
}
