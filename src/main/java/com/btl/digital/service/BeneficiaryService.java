package com.btl.digital.service;

import com.btl.digital.dto.request.CreateBeneficiaryRequest;
import com.btl.digital.dto.request.UpdateBeneficiaryRequest;
import com.btl.digital.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {

    List<BeneficiaryResponse> getMyBeneficiaries();

    BeneficiaryResponse getBeneficiary(Long id);

    BeneficiaryResponse createBeneficiary(CreateBeneficiaryRequest request);

    BeneficiaryResponse updateBeneficiary(Long id, UpdateBeneficiaryRequest request);

    void deleteBeneficiary(Long id);
}
