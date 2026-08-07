package com.btl.digital.mock.controller;

import com.btl.digital.mock.common.ApiResponse;
import com.btl.digital.mock.dto.account.AccountTransactionsResponse;
import com.btl.digital.mock.dto.account.CustomerAccount;
import com.btl.digital.mock.service.MockAccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class MockAccountController {

    private final MockAccountService accountService;

    public MockAccountController(MockAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerAccount>>> getMyAccounts() {
        return ResponseEntity.ok(ApiResponse.success("Customer accounts fetched successfully.", accountService.getMyAccounts()));
    }

    @GetMapping("/{iban}/transactions")
    public ResponseEntity<ApiResponse<AccountTransactionsResponse>> getAccountTransactions(
            @PathVariable String iban,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer pageStart,
            @RequestParam(required = false) String pageToken) {
        AccountTransactionsResponse data = accountService.getTransactions(iban, fromDate, toDate, pageSize, pageStart, pageToken);
        return ResponseEntity.ok(ApiResponse.success("Account transactions fetched successfully.", data));
    }
}