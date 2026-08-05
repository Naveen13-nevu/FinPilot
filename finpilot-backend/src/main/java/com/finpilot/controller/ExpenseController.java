package com.finpilot.controller;

import com.finpilot.dto.request.ExpenseRequest;
import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.ExpenseResponse;
import com.finpilot.dto.response.PageResponse;
import com.finpilot.entity.User;
import com.finpilot.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense", description = "Manage expense transactions")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Record a new expense transaction")
    public ResponseEntity<ApiResponse<ExpenseResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense recorded successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense transaction")
    public ApiResponse<ExpenseResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequest request) {
        return ApiResponse.success("Expense updated successfully", expenseService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense transaction")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        expenseService.delete(currentUser, id);
        return ApiResponse.message("Expense deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expense transaction by id")
    public ApiResponse<ExpenseResponse> getById(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(expenseService.getById(currentUser, id));
    }

    @GetMapping
    @Operation(summary = "Search, filter, sort and paginate expense transactions")
    public ApiResponse<PageResponse<ExpenseResponse>> search(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute TransactionSearchRequest filter) {
        return ApiResponse.success(expenseService.search(currentUser, filter));
    }
}