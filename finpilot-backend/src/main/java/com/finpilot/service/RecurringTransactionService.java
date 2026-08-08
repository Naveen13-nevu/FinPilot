package com.finpilot.service;

import com.finpilot.dto.request.RecurringTransactionRequest;
import com.finpilot.dto.response.RecurringTransactionResponse;
import com.finpilot.entity.User;

import java.util.List;
import java.util.UUID;

public interface RecurringTransactionService {

    RecurringTransactionResponse create(User currentUser, RecurringTransactionRequest request);

    RecurringTransactionResponse update(User currentUser, UUID id, RecurringTransactionRequest request);

    void delete(User currentUser, UUID id);

    RecurringTransactionResponse setActive(User currentUser, UUID id, boolean active);

    List<RecurringTransactionResponse> getAll(User currentUser);
}