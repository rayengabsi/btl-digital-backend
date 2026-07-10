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
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String description;
    private LocalDateTime transactionDate;
    private Long accountId;
}
