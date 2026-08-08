package com.finpilot.controller;

import com.finpilot.dto.request.BudgetRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.BudgetResponse;
import com.finpilot.entity.User;
import com.finpilot.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget Planner", description = "Manage monthly per-category budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create a monthly budget for a category")
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public ApiResponse<BudgetResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        return ApiResponse.success("Budget updated successfully", budgetService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        budgetService.delete(currentUser, id);
        return ApiResponse.message("Budget deleted successfully");
    }

    @GetMapping
    @Operation(summary = "Get all budgets for a given month (defaults to current month) with live spend tracking")
    public ApiResponse<List<BudgetResponse>> getByMonth(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        LocalDate resolvedMonth = month != null ? month : YearMonth.now().atDay(1);
        return ApiResponse.success(budgetService.getByMonth(currentUser, resolvedMonth));
    }
}