package com.btl.digital.mock.dto.dev;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Development-only request to credit fake money to the authenticated
 * user's primary mock account.
 */
@Data
public class DevAccountDepositRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.001", message = "Amount must be greater than zero")
    private BigDecimal amount;
}
