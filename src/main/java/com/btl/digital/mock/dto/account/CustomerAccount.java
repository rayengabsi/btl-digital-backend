package com.btl.digital.mock.dto.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CustomerAccount {
    private String accountId;
    private String arrangementId;
    private String productName;
    private String iban;
    private String currencyId;
    private BigDecimal availableBalance;
    private BigDecimal workingBalance;
}