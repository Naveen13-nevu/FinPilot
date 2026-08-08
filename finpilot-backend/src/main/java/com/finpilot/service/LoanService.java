package com.finpilot.service;

import com.finpilot.dto.request.LoanRequest;
import com.finpilot.dto.response.EmiScheduleEntryResponse;
import com.finpilot.dto.response.LoanResponse;
import com.finpilot.entity.User;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    LoanResponse create(User currentUser, LoanRequest request);

    LoanResponse update(User currentUser, UUID loanId, LoanRequest request);

    void delete(User currentUser, UUID loanId);

    LoanResponse getById(User currentUser, UUID loanId);

    List<LoanResponse> getAll(User currentUser);

    List<EmiScheduleEntryResponse> getAmortizationSchedule(User currentUser, UUID loanId);

    LoanResponse recordEmiPayment(User currentUser, UUID loanId);
}