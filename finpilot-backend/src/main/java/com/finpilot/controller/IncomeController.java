package com.finpilot.controller;

import com.finpilot.dto.request.IncomeRequest;
import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.IncomeResponse;
import com.finpilot.dto.response.PageResponse;
import com.finpilot.entity.User;
import com.finpilot.service.IncomeService;
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
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
@Tag(name = "Income", description = "Manage income transactions")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    @Operation(summary = "Record a new income transaction")
    public ResponseEntity<ApiResponse<IncomeResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody IncomeRequest request) {
        IncomeResponse response = incomeService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Income recorded successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an income transaction")
    public ApiResponse<IncomeResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody IncomeRequest request) {
        return ApiResponse.success("Income updated successfully", incomeService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an income transaction")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        incomeService.delete(currentUser, id);
        return ApiResponse.message("Income deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an income transaction by id")
    public ApiResponse<IncomeResponse> getById(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(incomeService.getById(currentUser, id));
    }

    @GetMapping
    @Operation(summary = "Search, filter, sort and paginate income transactions")
    public ApiResponse<PageResponse<IncomeResponse>> search(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute TransactionSearchRequest filter) {
        return ApiResponse.success(incomeService.search(currentUser, filter));
    }
}