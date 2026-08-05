package com.finpilot.service;

import com.finpilot.dto.request.ExpenseRequest;
import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.dto.response.ExpenseResponse;
import com.finpilot.dto.response.PageResponse;
import com.finpilot.entity.User;

import java.util.UUID;

public interface ExpenseService {

    ExpenseResponse create(User currentUser, ExpenseRequest request);

    ExpenseResponse update(User currentUser, UUID expenseId, ExpenseRequest request);

    void delete(User currentUser, UUID expenseId);

    ExpenseResponse getById(User currentUser, UUID expenseId);

    PageResponse<ExpenseResponse> search(User currentUser, TransactionSearchRequest filter);
}