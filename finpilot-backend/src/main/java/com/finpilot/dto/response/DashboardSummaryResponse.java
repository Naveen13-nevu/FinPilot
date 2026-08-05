package com.finpilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal currentBalance;
    private BigDecimal monthlyBudget;
    private BigDecimal budgetRemaining;
    private BigDecimal budgetUtilizationPercent;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}