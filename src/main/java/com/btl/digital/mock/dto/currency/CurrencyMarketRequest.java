package com.btl.digital.mock.dto.currency;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CurrencyMarketRequest {
    @NotEmpty(message = "At least one currency pair is required")
    @Valid
    private List<CurrencyItem> currencies;

    @Data
    public static class CurrencyItem {
        @NotBlank(message = "Currency ID is required")
        private String currencyId;

        @NotBlank(message = "Market is required")
        private String market;
    }
}