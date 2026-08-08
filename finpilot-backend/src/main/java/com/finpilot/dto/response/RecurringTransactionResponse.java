package com.finpilot.dto.response;

import com.finpilot.entity.CategoryType;
import com.finpilot.entity.PaymentMethod;
import com.finpilot.entity.RecurrenceFrequency;
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
public class RecurringTransactionResponse {

    private UUID id;
    private CategoryResponse category;
    private CategoryType type;
    private BigDecimal amount;
    private String description;
    private RecurrenceFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextRunDate;
    private PaymentMethod paymentMethod;
    private Boolean active;
}