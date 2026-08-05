package com.finpilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentTransactionResponse {

    private UUID id;
    private String type; // INCOME or EXPENSE
    private String categoryName;
    private String categoryColor;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
}