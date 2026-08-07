package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A fake T24 statement transaction line.
 */
@Data
@Builder
public class MockTransaction {

    private String accountId;
    private String transactionReference;
    private String transactionCode;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private BigDecimal closingBalance;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private java.util.List<String> narratives;
}