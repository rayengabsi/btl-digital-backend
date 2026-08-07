package com.btl.digital.mock.dto.transfer;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * The fully processed intra-bank transfer order returned after OTP
 * confirmation, mirroring the documented T24-shaped response.
 */
@Data
@Builder
public class IntraTransferResponse {
    private String transactionId;
    private String status;
    private String transactionStatus;
    private String transactionType;
    private String debitAccountId;
    private String debitAccountIban;
    private String debitCurrency;
    private BigDecimal debitAmount;
    private String debitValueDate;
    private String creditAccountId;
    private String creditAccountIban;
    private String creditCurrencyId;
    private String creditValueDate;
    private String commissionCode;
    private String profitCentreCustomer;
    private List<PaymentDetailResponse> paymentDetails;
    private List<OverrideDetailResponse> overrideDetails;

    @Data
    @Builder
    public static class PaymentDetailResponse {
        private String paymentDetail;
    }

    @Data
    @Builder
    public static class OverrideDetailResponse {
        private String code;
        private String id;
        private String type;
        private String description;
        private List<String> options;
    }
}