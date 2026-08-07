package com.btl.digital.mock.service;

import com.btl.digital.mock.common.MockApiException;
import com.btl.digital.mock.dto.beneficiary.BeneficiaryRequest;
import com.btl.digital.mock.dto.beneficiary.BeneficiaryResponse;
import com.btl.digital.mock.model.MockBeneficiary;
import com.btl.digital.mock.model.MockClientUser;
import com.btl.digital.mock.security.MockAuthContext;
import com.btl.digital.mock.store.MockDataStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MockBeneficiaryService {

    private final MockDataStore store;
    private final MockAuthContext authContext;

    public MockBeneficiaryService(MockDataStore store, MockAuthContext authContext) {
        this.store = store;
        this.authContext = authContext;
    }

    public BeneficiaryResponse create(BeneficiaryRequest request) {
        MockClientUser client = authContext.requireClient();

        if (!RibValidator.isValid(request.getRibNumber())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_RIB", "The RIB format or key is invalid.");
        }
        if (store.clientHasRib(client.getId(), request.getRibNumber())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "RIB_ALREADY_EXISTS", "RIB already exists.");
        }
        if (store.accountsForClient(client.getId()).stream().anyMatch(a -> a.getIban().endsWith(request.getRibNumber()))) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "SELF_BENEFICIARY_NOT_ALLOWED",
                    "The RIB belongs to the connected client.");
        }

        MockBeneficiary b = MockBeneficiary.builder()
                .id(store.nextBeneficiaryId())
                .clientUserId(client.getId())
                .beneficiaryName(request.getBeneficiaryName())
                .ribNumber(request.getRibNumber())
                .createdAt(LocalDateTime.now())
                .build();
        store.addBeneficiary(b);
        return toDto(b);
    }

    public List<BeneficiaryResponse> getAll() {
        MockClientUser client = authContext.requireClient();
        return store.beneficiariesForClient(client.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    public BeneficiaryResponse get(Long id) {
        MockClientUser client = authContext.requireClient();
        return store.beneficiaryById(id)
                .filter(b -> b.getClientUserId().equals(client.getId()))
                .map(this::toDto)
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND",
                        "Beneficiary does not exist or does not belong to the connected client."));
    }

    public BeneficiaryResponse update(Long id, BeneficiaryRequest request) {
        MockClientUser client = authContext.requireClient();
        MockBeneficiary existing = store.beneficiaryById(id)
                .filter(b -> b.getClientUserId().equals(client.getId()))
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND",
                        "Beneficiary does not exist or does not belong to the connected client."));

        if (!RibValidator.isValid(request.getRibNumber())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "INVALID_RIB", "The RIB format or key is invalid.");
        }
        if (store.clientHasRib(client.getId(), request.getRibNumber())
                && !existing.getRibNumber().equals(request.getRibNumber())) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "RIB_ALREADY_EXISTS", "RIB already exists.");
        }
        if (store.accountsForClient(client.getId()).stream().anyMatch(a -> a.getIban().endsWith(request.getRibNumber()))) {
            throw new MockApiException(HttpStatus.BAD_REQUEST, "SELF_BENEFICIARY_NOT_ALLOWED",
                    "The RIB belongs to the connected client.");
        }

        existing.setBeneficiaryName(request.getBeneficiaryName());
        existing.setRibNumber(request.getRibNumber());
        store.updateBeneficiary(existing);
        return toDto(existing);
    }

    public void delete(Long id) {
        MockClientUser client = authContext.requireClient();
        MockBeneficiary existing = store.beneficiaryById(id)
                .filter(b -> b.getClientUserId().equals(client.getId()))
                .orElseThrow(() -> new MockApiException(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND",
                        "Beneficiary does not exist or does not belong to the connected client."));
        store.removeBeneficiary(client.getId(), existing.getId());
    }

    private BeneficiaryResponse toDto(MockBeneficiary b) {
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .beneficiaryName(b.getBeneficiaryName())
                .ribNumber(b.getRibNumber())
                .build();
    }
}