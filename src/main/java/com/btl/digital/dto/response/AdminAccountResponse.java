package com.btl.digital.dto.response;

import com.btl.digital.enums.AccountStatus;
import com.btl.digital.enums.AccountType;
import com.btl.digital.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAccountResponse {
    private Long id;
    private String accountNumber;
    private String iban;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private AccountStatus status;
    private Long ownerId;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
