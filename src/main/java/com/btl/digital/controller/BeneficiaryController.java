package com.btl.digital.controller;

import com.btl.digital.dto.request.CreateBeneficiaryRequest;
import com.btl.digital.dto.request.UpdateBeneficiaryRequest;
import com.btl.digital.dto.response.BeneficiaryResponse;
import com.btl.digital.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(@Valid @RequestBody CreateBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.createBeneficiary(request));
    }

    @GetMapping
    public ResponseEntity<List<BeneficiaryResponse>> getMyBeneficiaries() {
        return ResponseEntity.ok(beneficiaryService.getMyBeneficiaries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiary(@PathVariable Long id) {
        return ResponseEntity.ok(beneficiaryService.getBeneficiary(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.updateBeneficiary(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(@PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.ok().build();
    }
}
