package com.btl.digital.mock.dto.transfer;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Transfer demand history record returned without an envelope.
 */
@Data
@Builder
public class TransferDemandResponse {
    private Long id;
    private Long clientUserId;
    private BigDecimal amount;
    private String transactionType;
    private String debitAccountId;
    private String debitCurrency;
    private String creditAccountId;
    private List<String> paymentDetails;
    private String status;
    private String createdAt;
    private String completedAt;
}