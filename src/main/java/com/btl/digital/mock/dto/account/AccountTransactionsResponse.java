package com.btl.digital.mock.dto.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Full account statement response mirroring the documented contract.
 */
@Data
@Builder
public class AccountTransactionsResponse {

    private Summary summary;
    private Pagination pagination;
    private List<Transaction> transactions;

    @Data
    @Builder
    public static class Summary {
        private String startDate;
        private String endDate;
        private String todayDate;
        private String accountTitle;
        private String accountType;
        private String currency;
        private BigDecimal totalAmount;
        private BigDecimal totalCredits;
        private BigDecimal totalDebits;
        private String street;
        private String addressLine1;
        private String addressLine2;
        private String postCode;
    }

    @Data
    @Builder
    public static class Pagination {
        private Integer pageStart;
        private Integer pageSize;
        private Integer totalSize;
        private String pageToken;
    }

    @Data
    @Builder
    public static class Transaction {
        private String accountId;
        private String transactionReference;
        private String transactionCode;
        private String bookingDate;
        private String valueDate;
        private BigDecimal closingBalance;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
        private List<String> narratives;
    }
}