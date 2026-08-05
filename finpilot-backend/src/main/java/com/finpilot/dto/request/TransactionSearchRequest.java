package com.finpilot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchRequest {

    private String keyword;
    private UUID categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String paymentMethod;

    private int page = 0;
    private int size = 20;
    private String sortBy = "transactionDate";
    private String sortDirection = "DESC";
}