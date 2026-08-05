package com.finpilot.controller;

import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.CategoryBreakdownResponse;
import com.finpilot.dto.response.DashboardSummaryResponse;
import com.finpilot.dto.response.MonthlyTrendResponse;
import com.finpilot.dto.response.RecentTransactionResponse;
import com.finpilot.entity.User;
import com.finpilot.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated financial overview and chart data")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get income/expense/balance/budget summary for a period (defaults to current month)")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate resolvedStart = startDate != null ? startDate : YearMonth.now().atDay(1);
        LocalDate resolvedEnd = endDate != null ? endDate : YearMonth.now().atEndOfMonth();

        return ApiResponse.success(dashboardService.getSummary(currentUser, resolvedStart, resolvedEnd));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get monthly income vs expense trend for charts")
    public ApiResponse<List<MonthlyTrendResponse>> getMonthlyTrend(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "6") int months) {
        return ApiResponse.success(dashboardService.getMonthlyTrend(currentUser, months));
    }

    @GetMapping("/expense-breakdown")
    @Operation(summary = "Get expense breakdown by category for a period (defaults to current month)")
    public ApiResponse<List<CategoryBreakdownResponse>> getExpenseBreakdown(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate resolvedStart = startDate != null ? startDate : YearMonth.now().atDay(1);
        LocalDate resolvedEnd = endDate != null ? endDate : YearMonth.now().atEndOfMonth();

        return ApiResponse.success(dashboardService.getExpenseBreakdown(currentUser, resolvedStart, resolvedEnd));
    }

    @GetMapping("/recent-transactions")
    @Operation(summary = "Get the most recent income and expense transactions combined")
    public ApiResponse<List<RecentTransactionResponse>> getRecentTransactions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(dashboardService.getRecentTransactions(currentUser, limit));
    }
}