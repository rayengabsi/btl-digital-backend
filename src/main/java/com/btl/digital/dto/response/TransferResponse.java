package com.btl.digital.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String message;
    private LocalDateTime transactionDate;
}
