package com.btl.digital.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long totalAccounts;
    private Long activeAccounts;
    private Long frozenAccounts;
    private Long totalTransactions;
    private BigDecimal totalBankBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalTransfers;
}
