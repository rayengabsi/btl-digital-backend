package com.btl.digital.mock.controller;

import com.btl.digital.mock.common.ApiResponse;
import com.btl.digital.mock.dto.currency.CurrencyMarketRequest;
import com.btl.digital.mock.dto.currency.CurrencyMarketResponse;
import com.btl.digital.mock.service.MockCurrencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
public class MockCurrencyController {

    private final MockCurrencyService currencyService;

    public MockCurrencyController(MockCurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping("/rates")
    public ResponseEntity<ApiResponse<List<CurrencyMarketResponse>>> getRates(@Valid @RequestBody CurrencyMarketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Currency markets fetched successfully.", currencyService.getRates(request)));
    }
}