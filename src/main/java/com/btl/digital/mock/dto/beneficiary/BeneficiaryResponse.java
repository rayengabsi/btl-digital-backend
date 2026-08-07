package com.btl.digital.mock.dto.beneficiary;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaryResponse {
    private Long id;
    private String beneficiaryName;
    private String ribNumber;
}