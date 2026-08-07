package com.btl.digital.mock.dto.currency;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CurrencyMarketResponse {
    private String currencyId;
    private String displayName;
    private BigDecimal sellMarket;
    private BigDecimal buyMarket;
}