package com.finpilot.service;

import com.finpilot.dto.request.IncomeRequest;
import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.dto.response.IncomeResponse;
import com.finpilot.dto.response.PageResponse;
import com.finpilot.entity.User;

import java.util.UUID;

public interface IncomeService {

    IncomeResponse create(User currentUser, IncomeRequest request);

    IncomeResponse update(User currentUser, UUID incomeId, IncomeRequest request);

    void delete(User currentUser, UUID incomeId);

    IncomeResponse getById(User currentUser, UUID incomeId);

    PageResponse<IncomeResponse> search(User currentUser, TransactionSearchRequest filter);
}