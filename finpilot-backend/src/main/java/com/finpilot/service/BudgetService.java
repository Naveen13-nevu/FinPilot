package com.finpilot.service;

import com.finpilot.dto.request.BudgetRequest;
import com.finpilot.dto.response.BudgetResponse;
import com.finpilot.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BudgetService {

    BudgetResponse create(User currentUser, BudgetRequest request);

    BudgetResponse update(User currentUser, UUID budgetId, BudgetRequest request);

    void delete(User currentUser, UUID budgetId);

    List<BudgetResponse> getByMonth(User currentUser, LocalDate month);
}