package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.currency.CurrencyMarketRequest;
import com.btl.digital.mock.dto.currency.CurrencyMarketResponse;
import com.btl.digital.mock.model.MockCurrencyRate;
import com.btl.digital.mock.security.MockAuthContext;
import com.btl.digital.mock.store.MockDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockCurrencyService {

    private final MockDataStore store;
    private final MockAuthContext authContext;

    public MockCurrencyService(MockDataStore store, MockAuthContext authContext) {
        this.store = store;
        this.authContext = authContext;
    }

    public List<CurrencyMarketResponse> getRates(CurrencyMarketRequest request) {
        authContext.requireClient();
        List<CurrencyMarketResponse> result = new ArrayList<>();

        for (CurrencyMarketRequest.CurrencyItem item : request.getCurrencies()) {
            String currencyId = item.getCurrencyId().trim().toUpperCase();
            String market = item.getMarket().trim();

            List<MockCurrencyRate> ratesForCurrency = store.currencyRates(currencyId);
            if (ratesForCurrency.isEmpty()) {
                throw new MockApiException(HttpStatus.BAD_REQUEST, "CURRENCY_NOT_FOUND",
                        "Currency " + currencyId + " is not available.");
            }

            MockCurrencyRate rate = ratesForCurrency.stream()
                    .filter(r -> r.getMarket().equals(market))
                    .findFirst()
                    .orElseThrow(() -> new MockApiException(HttpStatus.BAD_REQUEST, "CURRENCY_MARKET_NOT_FOUND",
                            "Market " + market + " not found for currency " + currencyId));

            result.add(CurrencyMarketResponse.builder()
                    .currencyId(rate.getCurrencyId())
                    .displayName(rate.getDisplayName())
                    .sellMarket(rate.getSellRate())
                    .buyMarket(rate.getBuyRate())
                    .build());
        }

        return result;
    }
}