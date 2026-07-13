package com.btl.digital.dto.request;

import com.btl.digital.enums.TransactionType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFilterRequest {
    private Long accountId;
    private TransactionType transactionType;
    private LocalDate fromDate;
    private LocalDate toDate;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;

    @Builder.Default
    private String sortBy = "transactionDate";

    @Builder.Default
    private String direction = "DESC";
}
