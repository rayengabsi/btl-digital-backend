package com.btl.digital.dto.response;

import com.btl.digital.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String type;
    private Long userId;
    private String email;
    private Role role;
}
