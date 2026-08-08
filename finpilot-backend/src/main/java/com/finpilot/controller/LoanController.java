package com.finpilot.controller;

import com.finpilot.dto.request.LoanRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.EmiScheduleEntryResponse;
import com.finpilot.dto.response.LoanResponse;
import com.finpilot.entity.User;
import com.finpilot.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loan & EMI Tracker", description = "Manage loans and track EMI payments")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Create a new loan (EMI is auto-calculated)")
    public ResponseEntity<ApiResponse<LoanResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody LoanRequest request) {
        LoanResponse response = loanService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update loan terms (only before any EMI has been paid)")
    public ApiResponse<LoanResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody LoanRequest request) {
        return ApiResponse.success("Loan updated successfully", loanService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        loanService.delete(currentUser, id);
        return ApiResponse.message("Loan deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by id")
    public ApiResponse<LoanResponse> getById(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(loanService.getById(currentUser, id));
    }

    @GetMapping
    @Operation(summary = "List all loans for the current user")
    public ApiResponse<List<LoanResponse>> getAll(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(loanService.getAll(currentUser));
    }

    @GetMapping("/{id}/schedule")
    @Operation(summary = "Get the full amortization schedule for a loan")
    public ApiResponse<List<EmiScheduleEntryResponse>> getSchedule(
            @AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(loanService.getAmortizationSchedule(currentUser, id));
    }

    @PostMapping("/{id}/pay-emi")
    @Operation(summary = "Record the next EMI payment for a loan")
    public ApiResponse<LoanResponse> recordEmiPayment(
            @AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success("EMI payment recorded successfully", loanService.recordEmiPayment(currentUser, id));
    }
}