package com.btl.digital.mock.controller;

import com.btl.digital.mock.common.ApiResponse;
import com.btl.digital.mock.dto.transfer.ConfirmIntraTransferRequest;
import com.btl.digital.mock.dto.transfer.IntraTransferInitiationResponse;
import com.btl.digital.mock.dto.transfer.IntraTransferRequest;
import com.btl.digital.mock.dto.transfer.IntraTransferResponse;
import com.btl.digital.mock.dto.transfer.PageResponse;
import com.btl.digital.mock.dto.transfer.TransferDemandResponse;
import com.btl.digital.mock.service.MockTransferService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class MockTransferController {

    private final MockTransferService transferService;

    public MockTransferController(MockTransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/api/v1/transfers/intra")
    public ResponseEntity<ApiResponse<IntraTransferInitiationResponse>> initiate(@Valid @RequestBody IntraTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer OTP sent.", transferService.initiate(request)));
    }

    @PostMapping("/api/v1/transfers/intra/confirm")
    public ResponseEntity<ApiResponse<IntraTransferResponse>> confirm(@Valid @RequestBody ConfirmIntraTransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer order processed.", transferService.confirm(request)));
    }

    @GetMapping("/api/v1/transfer-demands")
    public ResponseEntity<PageResponse<TransferDemandResponse>> history(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(transferService.getHistory(page, size, from, to));
    }

    @GetMapping("/api/v1/transfer-demands/{id}")
    public ResponseEntity<TransferDemandResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getById(id));
    }
}