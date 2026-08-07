package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A pending/completed intra-bank transfer demand.
 */
@Data
@Builder
public class MockTransferDemand {

    private Long id;
    private Long clientUserId;
    private BigDecimal amount;
    private String transactionType;
    private String debitAccountId;
    private String debitCurrency;
    private String creditAccountId;
    private List<String> paymentDetails;
    /** PENDING, SUCCESSFUL, FAILED */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String otp;
    private LocalDateTime otpExpiresAt;
    private int otpAttempts;
    private String transactionId;
}