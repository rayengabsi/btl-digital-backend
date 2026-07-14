package com.btl.digital.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {
    private Long id;
    private String nickname;
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String beneficiaryIban;
    private LocalDateTime createdAt;
}
