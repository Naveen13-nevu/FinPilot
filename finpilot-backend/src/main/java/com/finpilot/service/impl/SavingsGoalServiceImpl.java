package com.finpilot.service.impl;

import com.finpilot.dto.request.GoalContributionRequest;
import com.finpilot.dto.request.SavingsGoalRequest;
import com.finpilot.dto.response.SavingsGoalResponse;
import com.finpilot.entity.GoalStatus;
import com.finpilot.entity.NotificationType;
import com.finpilot.entity.SavingsGoal;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.SavingsGoalMapper;
import com.finpilot.repository.SavingsGoalRepository;
import com.finpilot.service.NotificationService;
import com.finpilot.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsGoalMapper savingsGoalMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SavingsGoalResponse create(User currentUser, SavingsGoalRequest request) {
        SavingsGoal goal = SavingsGoal.builder()
                .user(currentUser)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .icon(request.getIcon())
                .color(request.getColor())
                .status(GoalStatus.ACTIVE)
                .build();

        return savingsGoalMapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public SavingsGoalResponse update(User currentUser, UUID goalId, SavingsGoalRequest request) {
        SavingsGoal goal = getOwnedGoal(currentUser, goalId);

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setIcon(request.getIcon());
        goal.setColor(request.getColor());

        refreshStatus(goal);

        return savingsGoalMapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID goalId) {
        SavingsGoal goal = getOwnedGoal(currentUser, goalId);
        savingsGoalRepository.delete(goal);
    }

    @Override
    @Transactional
    public SavingsGoalResponse contribute(User currentUser, UUID goalId, GoalContributionRequest request) {
        SavingsGoal goal = getOwnedGoal(currentUser, goalId);

        if (goal.getStatus() == GoalStatus.ABANDONED) {
            throw new BadRequestException("Cannot contribute to an abandoned goal");
        }

        boolean wasAlreadyCompleted = goal.getStatus() == GoalStatus.COMPLETED;

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
        refreshStatus(goal);

        SavingsGoal saved = savingsGoalRepository.save(goal);

        if (!wasAlreadyCompleted && saved.getStatus() == GoalStatus.COMPLETED) {
            notificationService.notify(
                    currentUser,
                    NotificationType.GOAL_ACHIEVED,
                    "Goal achieved: " + saved.getName(),
                    "Congratulations! You've reached your savings goal of "
                            + currentUser.getCurrency() + " " + saved.getTargetAmount() + ".",
                    saved.getId());
        }

        return savingsGoalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SavingsGoalResponse withdraw(User currentUser, UUID goalId, GoalContributionRequest request) {
        SavingsGoal goal = getOwnedGoal(currentUser, goalId);

        if (goal.getCurrentAmount().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Withdrawal amount exceeds current saved amount");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().subtract(request.getAmount()));
        refreshStatus(goal);

        return savingsGoalMapper.toResponse(savingsGoalRepository.save(goal));
    }

    @Override
    public List<SavingsGoalResponse> getAll(User currentUser) {
        return savingsGoalRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(savingsGoalMapper::toResponse)
                .toList();
    }

    private void refreshStatus(SavingsGoal goal) {
        if (goal.getStatus() != GoalStatus.ABANDONED) {
            goal.setStatus(goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0
                    ? GoalStatus.COMPLETED
                    : GoalStatus.ACTIVE);
        }
    }

    private SavingsGoal getOwnedGoal(User currentUser, UUID goalId) {
        return savingsGoalRepository.findByIdAndUser(goalId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", "id", goalId));
    }
}