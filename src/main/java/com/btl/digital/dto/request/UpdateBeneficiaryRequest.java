package com.btl.digital.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBeneficiaryRequest {

    @NotBlank(message = "Nickname is required")
    private String nickname;
}
