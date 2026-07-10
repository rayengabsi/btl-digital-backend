package com.btl.digital.dto.request;

import com.btl.digital.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotNull
    private Long ownerId;

    @NotNull
    private AccountType accountType;
}
