package com.finpilot.service.impl;

import com.finpilot.dto.request.BudgetRequest;
import com.finpilot.dto.response.BudgetResponse;
import com.finpilot.entity.Budget;
import com.finpilot.entity.Category;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.NotificationType;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.DuplicateResourceException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.CategoryMapper;
import com.finpilot.repository.BudgetRepository;
import com.finpilot.repository.CategoryRepository;
import com.finpilot.repository.ExpenseRepository;
import com.finpilot.service.BudgetService;
import com.finpilot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryMapper categoryMapper;
    private final NotificationService notificationService;
    private final com.finpilot.repository.NotificationRepository notificationRepository;

    @Override
    @Transactional
    public BudgetResponse create(User currentUser, BudgetRequest request) {
        Category category = resolveExpenseCategory(currentUser, request.getCategoryId());
        LocalDate normalizedMonth = YearMonth.from(request.getBudgetMonth()).atDay(1);

        if (budgetRepository.existsByUserAndCategoryIdAndBudgetMonth(currentUser, category.getId(), normalizedMonth)) {
            throw new DuplicateResourceException("A budget for this category already exists for the selected month");
        }

        Budget budget = Budget.builder()
                .user(currentUser)
                .category(category)
                .budgetMonth(normalizedMonth)
                .amount(request.getAmount())
                .alertEnabled(request.getAlertEnabled() == null || request.getAlertEnabled())
                .alertThresholdPercent(request.getAlertThresholdPercent() != null ? request.getAlertThresholdPercent() : 80)
                .build();

        return toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public BudgetResponse update(User currentUser, UUID budgetId, BudgetRequest request) {
        Budget budget = getOwnedBudget(currentUser, budgetId);
        Category category = resolveExpenseCategory(currentUser, request.getCategoryId());

        budget.setCategory(category);
        budget.setBudgetMonth(YearMonth.from(request.getBudgetMonth()).atDay(1));
        budget.setAmount(request.getAmount());
        if (request.getAlertEnabled() != null) {
            budget.setAlertEnabled(request.getAlertEnabled());
        }
        if (request.getAlertThresholdPercent() != null) {
            budget.setAlertThresholdPercent(request.getAlertThresholdPercent());
        }

        return toResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID budgetId) {
        Budget budget = getOwnedBudget(currentUser, budgetId);
        budgetRepository.delete(budget);
    }

    @Override
    public List<BudgetResponse> getByMonth(User currentUser, LocalDate month) {
        LocalDate normalizedMonth = YearMonth.from(month).atDay(1);
        return budgetRepository.findByUserAndBudgetMonthOrderByCategoryNameAsc(currentUser, normalizedMonth)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BudgetResponse toResponse(Budget budget) {
        YearMonth ym = YearMonth.from(budget.getBudgetMonth());
        BigDecimal spent = expenseRepository.sumByUserAndCategoryAndDateRange(
                budget.getUser(), budget.getCategory().getId(), ym.atDay(1), ym.atEndOfMonth());

        BigDecimal remaining = budget.getAmount().subtract(spent);
        BigDecimal utilization = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean breached = budget.getAlertEnabled()
                && utilization.compareTo(BigDecimal.valueOf(budget.getAlertThresholdPercent())) >= 0;

        if (breached) {
            fireThresholdAlertIfNotAlreadySent(budget, utilization);
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(categoryMapper.toResponse(budget.getCategory()))
                .budgetMonth(budget.getBudgetMonth())
                .amount(budget.getAmount())
                .spentAmount(spent)
                .remainingAmount(remaining)
                .utilizationPercent(utilization)
                .alertEnabled(budget.getAlertEnabled())
                .alertThresholdPercent(budget.getAlertThresholdPercent())
                .thresholdBreached(breached)
                .build();
    }

    private void fireThresholdAlertIfNotAlreadySent(Budget budget, BigDecimal utilization) {
        boolean alreadySentToday = notificationRepository.existsByUserAndTypeAndRelatedEntityIdAndCreatedAtAfter(
                budget.getUser(), NotificationType.BUDGET_ALERT, budget.getId(), LocalDateTime.now().toLocalDate().atStartOfDay());

        if (!alreadySentToday) {
            notificationService.notify(
                    budget.getUser(),
                    NotificationType.BUDGET_ALERT,
                    "Budget alert: " + budget.getCategory().getName(),
                    "You've used " + utilization + "% of your " + budget.getCategory().getName()
                            + " budget for " + YearMonth.from(budget.getBudgetMonth()) + ".",
                    budget.getId());
        }
    }

    private Budget getOwnedBudget(User currentUser, UUID budgetId) {
        return budgetRepository.findByIdAndUser(budgetId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
    }

    private Category resolveExpenseCategory(User currentUser, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUser(categoryId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Budgets can only be set for expense categories");
        }

        return category;
    }
}