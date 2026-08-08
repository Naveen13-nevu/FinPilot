package com.finpilot.controller;

import com.finpilot.dto.request.GoalContributionRequest;
import com.finpilot.dto.request.SavingsGoalRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.SavingsGoalResponse;
import com.finpilot.entity.User;
import com.finpilot.service.SavingsGoalService;
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
@RequestMapping("/api/v1/savings-goals")
@RequiredArgsConstructor
@Tag(name = "Savings Goals", description = "Manage personal savings goals and contributions")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalResponse response = savingsGoalService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Savings goal created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a savings goal")
    public ApiResponse<SavingsGoalResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalRequest request) {
        return ApiResponse.success("Savings goal updated successfully", savingsGoalService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a savings goal")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        savingsGoalService.delete(currentUser, id);
        return ApiResponse.message("Savings goal deleted successfully");
    }

    @PostMapping("/{id}/contribute")
    @Operation(summary = "Add money towards a savings goal")
    public ApiResponse<SavingsGoalResponse> contribute(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody GoalContributionRequest request) {
        return ApiResponse.success("Contribution added successfully", savingsGoalService.contribute(currentUser, id, request));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw money from a savings goal")
    public ApiResponse<SavingsGoalResponse> withdraw(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody GoalContributionRequest request) {
        return ApiResponse.success("Withdrawal processed successfully", savingsGoalService.withdraw(currentUser, id, request));
    }

    @GetMapping
    @Operation(summary = "Get all savings goals for the current user")
    public ApiResponse<List<SavingsGoalResponse>> getAll(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(savingsGoalService.getAll(currentUser));
    }
}