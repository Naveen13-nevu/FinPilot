package com.finpilot.controller;

import com.finpilot.dto.request.RecurringTransactionRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.RecurringTransactionResponse;
import com.finpilot.entity.User;
import com.finpilot.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
@Tag(name = "Recurring Transactions", description = "Manage automatically repeating income and expense entries")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    @Operation(summary = "Create a new recurring transaction")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RecurringTransactionRequest request) {
        RecurringTransactionResponse response = recurringTransactionService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring transaction created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a recurring transaction")
    public ApiResponse<RecurringTransactionResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ApiResponse.success("Recurring transaction updated successfully",
                recurringTransactionService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recurring transaction")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        recurringTransactionService.delete(currentUser, id);
        return ApiResponse.message("Recurring transaction deleted successfully");
    }

    @PatchMapping("/{id}/pause")
    @Operation(summary = "Pause a recurring transaction")
    public ApiResponse<RecurringTransactionResponse> pause(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(recurringTransactionService.setActive(currentUser, id, false));
    }

    @PatchMapping("/{id}/resume")
    @Operation(summary = "Resume a paused recurring transaction")
    public ApiResponse<RecurringTransactionResponse> resume(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(recurringTransactionService.setActive(currentUser, id, true));
    }

    @GetMapping
    @Operation(summary = "List all recurring transactions for the current user")
    public ApiResponse<List<RecurringTransactionResponse>> getAll(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(recurringTransactionService.getAll(currentUser));
    }
}