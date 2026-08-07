package com.btl.digital.mock.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * A fake currency buy/sell rate for a given market.
 */
@Data
@Builder
public class MockCurrencyRate {

    private String currencyId;
    private String displayName;
    private String market;
    private BigDecimal sellRate;
    private BigDecimal buyRate;
}