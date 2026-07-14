package com.btl.digital.service.impl;

import com.btl.digital.dto.request.CreateBeneficiaryRequest;
import com.btl.digital.dto.request.UpdateBeneficiaryRequest;
import com.btl.digital.dto.response.BeneficiaryResponse;
import com.btl.digital.entity.Beneficiary;
import com.btl.digital.entity.User;
import com.btl.digital.exception.AccessDeniedException;
import com.btl.digital.exception.ResourceNotFoundException;
import com.btl.digital.repository.BeneficiaryRepository;
import com.btl.digital.repository.UserRepository;
import com.btl.digital.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    @Override
    public List<BeneficiaryResponse> getMyBeneficiaries() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Beneficiary> beneficiaries = beneficiaryRepository.findByOwnerOrderByCreatedAtDesc(currentUser);
        return beneficiaries.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BeneficiaryResponse getBeneficiary(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        return mapToResponse(beneficiary);
    }

    @Override
    @Transactional
    public BeneficiaryResponse createBeneficiary(CreateBeneficiaryRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        boolean exists = beneficiaryRepository.existsByOwnerAndBeneficiaryAccountNumber(
                currentUser, request.getBeneficiaryAccountNumber()
        );
        if (exists) {
            throw new IllegalArgumentException("Beneficiary already exists");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .nickname(request.getNickname())
                .beneficiaryName(request.getBeneficiaryName())
                .beneficiaryAccountNumber(request.getBeneficiaryAccountNumber())
                .beneficiaryIban(request.getBeneficiaryIban())
                .owner(currentUser)
                .build();

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);
        return mapToResponse(savedBeneficiary);
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiary(Long id, UpdateBeneficiaryRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        beneficiary.setNickname(request.getNickname());
        Beneficiary updatedBeneficiary = beneficiaryRepository.save(beneficiary);
        return mapToResponse(updatedBeneficiary);
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        beneficiaryRepository.delete(beneficiary);
    }

    private User getCurrentAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Access denied");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BeneficiaryResponse mapToResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .nickname(beneficiary.getNickname())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .beneficiaryAccountNumber(beneficiary.getBeneficiaryAccountNumber())
                .beneficiaryIban(beneficiary.getBeneficiaryIban())
                .createdAt(beneficiary.getCreatedAt())
                .build();
    }
}
