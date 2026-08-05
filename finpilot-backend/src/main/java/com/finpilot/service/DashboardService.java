package com.finpilot.service;

import com.finpilot.dto.response.CategoryBreakdownResponse;
import com.finpilot.dto.response.DashboardSummaryResponse;
import com.finpilot.dto.response.MonthlyTrendResponse;
import com.finpilot.dto.response.RecentTransactionResponse;
import com.finpilot.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getSummary(User currentUser, LocalDate startDate, LocalDate endDate);

    List<MonthlyTrendResponse> getMonthlyTrend(User currentUser, int months);

    List<CategoryBreakdownResponse> getExpenseBreakdown(User currentUser, LocalDate startDate, LocalDate endDate);

    List<RecentTransactionResponse> getRecentTransactions(User currentUser, int limit);
}