package com.btl.digital.mock.dto.transfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntraTransferInitiationResponse {
    private Long transferDemandId;
    private String status;
    private String otpExpiresAt;
}