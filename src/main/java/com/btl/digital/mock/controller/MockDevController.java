package com.btl.digital.mock.controller;

import com.btl.digital.mock.common.ApiResponse;
import com.btl.digital.mock.dto.dev.DevAccountDepositRequest;
import com.btl.digital.mock.dto.dev.DevAccountDepositResponse;
import com.btl.digital.mock.service.MockAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DEVELOPMENT-ONLY mock endpoints for manual testing (e.g. crediting fake
 * money to a logged-in user's account to test transfers). Not part of the
 * documented banking API.
 */
@RestController
@RequestMapping("/api/v1/dev")
public class MockDevController {

    private final MockAccountService accountService;

    public MockDevController(MockAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts/{iban}/deposit")
    public ResponseEntity<ApiResponse<DevAccountDepositResponse>> deposit(
            @PathVariable String iban,
            @Valid @RequestBody DevAccountDepositRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deposit processed.", accountService.depositToAccount(iban, request)));
    }
}
