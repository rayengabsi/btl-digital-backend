package com.btl.digital.mock.controller;

import com.btl.digital.mock.dto.beneficiary.BeneficiaryRequest;
import com.btl.digital.mock.dto.beneficiary.BeneficiaryResponse;
import com.btl.digital.mock.service.MockBeneficiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class MockBeneficiaryController {

    private final MockBeneficiaryService beneficiaryService;

    public MockBeneficiaryController(MockBeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> create(@Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getAll() {
        return ResponseEntity.ok(beneficiaryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(beneficiaryService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> update(@PathVariable Long id, @Valid @RequestBody BeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        beneficiaryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}