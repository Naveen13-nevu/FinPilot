package com.finpilot.service;

import com.finpilot.dto.request.GoalContributionRequest;
import com.finpilot.dto.request.SavingsGoalRequest;
import com.finpilot.dto.response.SavingsGoalResponse;
import com.finpilot.entity.User;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalService {

    SavingsGoalResponse create(User currentUser, SavingsGoalRequest request);

    SavingsGoalResponse update(User currentUser, UUID goalId, SavingsGoalRequest request);

    void delete(User currentUser, UUID goalId);

    SavingsGoalResponse contribute(User currentUser, UUID goalId, GoalContributionRequest request);

    SavingsGoalResponse withdraw(User currentUser, UUID goalId, GoalContributionRequest request);

    List<SavingsGoalResponse> getAll(User currentUser);
}