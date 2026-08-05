package com.finpilot.dto.response;

import com.finpilot.entity.PaymentMethod;
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
public class IncomeResponse {

    private UUID id;
    private CategoryResponse category;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private PaymentMethod paymentMethod;
    private String source;
    private Boolean isRecurring;
}