package com.btl.digital.dto.response;

import com.btl.digital.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String description;
    private Long accountId;
    private String accountNumber;
    private Long ownerId;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerEmail;
    private LocalDateTime transactionDate;
}
