package com.finpilot.service.impl;

import com.finpilot.dto.response.CategoryBreakdownResponse;
import com.finpilot.dto.response.DashboardSummaryResponse;
import com.finpilot.dto.response.MonthlyTrendResponse;
import com.finpilot.dto.response.RecentTransactionResponse;
import com.finpilot.entity.Expense;
import com.finpilot.entity.Income;
import com.finpilot.entity.User;
import com.finpilot.repository.ExpenseRepository;
import com.finpilot.repository.IncomeRepository;
import com.finpilot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter MONTH_KEY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public DashboardSummaryResponse getSummary(User currentUser, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = incomeRepository.sumByUserAndDateRange(currentUser, startDate, endDate);
        BigDecimal totalExpense = expenseRepository.sumByUserAndDateRange(currentUser, startDate, endDate);
        BigDecimal currentBalance = totalIncome.subtract(totalExpense);

        BigDecimal monthlyBudget = currentUser.getMonthlyBudget() != null
                ? currentUser.getMonthlyBudget()
                : BigDecimal.ZERO;

        BigDecimal budgetRemaining = monthlyBudget.subtract(totalExpense);

        BigDecimal budgetUtilizationPercent = BigDecimal.ZERO;
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUtilizationPercent = totalExpense
                    .divide(monthlyBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .currentBalance(currentBalance)
                .monthlyBudget(monthlyBudget)
                .budgetRemaining(budgetRemaining)
                .budgetUtilizationPercent(budgetUtilizationPercent)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }

    @Override
    public List<MonthlyTrendResponse> getMonthlyTrend(User currentUser, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = YearMonth.from(endDate).minusMonths(months - 1L).atDay(1);

        Map<String, BigDecimal> incomeByMonth = toMonthMap(incomeRepository.sumGroupByMonth(currentUser, startDate, endDate));
        Map<String, BigDecimal> expenseByMonth = toMonthMap(expenseRepository.sumGroupByMonth(currentUser, startDate, endDate));

        List<MonthlyTrendResponse> trend = new ArrayList<>();
        YearMonth cursor = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);

        while (!cursor.isAfter(last)) {
            String key = cursor.format(MONTH_KEY_FORMAT);
            BigDecimal income = incomeByMonth.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal expense = expenseByMonth.getOrDefault(key, BigDecimal.ZERO);

            trend.add(MonthlyTrendResponse.builder()
                    .month(key)
                    .income(income)
                    .expense(expense)
                    .net(income.subtract(expense))
                    .build());

            cursor = cursor.plusMonths(1);
        }

        return trend;
    }

    @Override
    public List<CategoryBreakdownResponse> getExpenseBreakdown(User currentUser, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = expenseRepository.sumGroupByCategory(currentUser, startDate, endDate);

        BigDecimal total = rows.stream()
                .map(row -> (BigDecimal) row[3])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdownResponse> breakdown = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal amount = (BigDecimal) row[3];
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            breakdown.add(CategoryBreakdownResponse.builder()
                    .categoryId((UUID) row[0])
                    .categoryName((String) row[1])
                    .color((String) row[2])
                    .amount(amount)
                    .percentage(percentage)
                    .build());
        }

        return breakdown;
    }

    @Override
    public List<RecentTransactionResponse> getRecentTransactions(User currentUser, int limit) {
        List<RecentTransactionResponse> combined = new ArrayList<>();

        for (Income income : incomeRepository.findTop10ByUserOrderByTransactionDateDesc(currentUser)) {
            combined.add(RecentTransactionResponse.builder()
                    .id(income.getId())
                    .type("INCOME")
                    .categoryName(income.getCategory().getName())
                    .categoryColor(income.getCategory().getColor())
                    .amount(income.getAmount())
                    .description(income.getDescription())
                    .transactionDate(income.getTransactionDate())
                    .build());
        }

        for (Expense expense : expenseRepository.findTop10ByUserOrderByTransactionDateDesc(currentUser)) {
            combined.add(RecentTransactionResponse.builder()
                    .id(expense.getId())
                    .type("EXPENSE")
                    .categoryName(expense.getCategory().getName())
                    .categoryColor(expense.getCategory().getColor())
                    .amount(expense.getAmount())
                    .description(expense.getDescription())
                    .transactionDate(expense.getTransactionDate())
                    .build());
        }

        return combined.stream()
                .sorted(Comparator.comparing(RecentTransactionResponse::getTransactionDate).reversed())
                .limit(limit)
                .toList();
    }

    private Map<String, BigDecimal> toMonthMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }
}