package com.btl.digital.controller;

import com.btl.digital.dto.request.DepositRequest;
import com.btl.digital.dto.request.TransferRequest;
import com.btl.digital.dto.request.WithdrawRequest;
import com.btl.digital.dto.request.TransactionFilterRequest;
import com.btl.digital.dto.response.TransactionResponse;
import com.btl.digital.dto.response.TransferResponse;
import com.btl.digital.enums.TransactionType;
import com.btl.digital.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(transactionService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(transactionService.withdraw(request));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        TransactionFilterRequest request = TransactionFilterRequest.builder()
                .accountId(accountId)
                .transactionType(transactionType)
                .fromDate(fromDate)
                .toDate(toDate)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(direction)
                .build();

        return ResponseEntity.ok(transactionService.getTransactions(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transfer(request));
    }
}
