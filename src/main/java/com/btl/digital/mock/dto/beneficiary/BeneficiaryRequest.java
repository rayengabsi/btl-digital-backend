package com.btl.digital.mock.dto.beneficiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BeneficiaryRequest {
    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotBlank(message = "RIB is required")
    @Pattern(regexp = "\\d{20}", message = "RIB must contain exactly 20 digits")
    private String ribNumber;
}