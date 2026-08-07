package com.btl.digital.mock.dto.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class IntraTransferRequest {
    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @NotBlank(message = "Debit account ID is required")
    private String debitAccountId;

    @NotBlank(message = "Debit currency is required")
    private String debitCurrency;

    @NotNull(message = "Debit amount is required")
    @DecimalMin(value = "0.001", message = "Debit amount must be greater than zero")
    private BigDecimal debitAmount;

    @NotBlank(message = "Credit account ID is required")
    private String creditAccountId;

    @NotEmpty(message = "Payment details are required")
    @Valid
    private List<PaymentDetail> paymentDetails;

    @Data
    public static class PaymentDetail {
        @NotBlank(message = "Payment detail is required")
        private String paymentDetail;
    }
}