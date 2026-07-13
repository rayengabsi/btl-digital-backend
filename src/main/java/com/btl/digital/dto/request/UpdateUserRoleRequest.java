package com.btl.digital.dto.request;

import com.btl.digital.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRoleRequest {
    @NotNull
    private Role role;
}
