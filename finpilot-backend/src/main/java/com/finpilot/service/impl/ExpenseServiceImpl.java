package com.finpilot.service.impl;

import com.finpilot.dto.request.ExpenseRequest;
import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.dto.response.ExpenseResponse;
import com.finpilot.dto.response.PageResponse;
import com.finpilot.entity.Category;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.Expense;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.ExpenseMapper;
import com.finpilot.repository.CategoryRepository;
import com.finpilot.repository.ExpenseRepository;
import com.finpilot.repository.spec.ExpenseSpecification;
import com.finpilot.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ExpenseResponse create(User currentUser, ExpenseRequest request) {
        Category category = resolveCategory(currentUser, request.getCategoryId());

        Expense expense = Expense.builder()
                .user(currentUser)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .paymentMethod(request.getPaymentMethod())
                .merchant(request.getMerchant())
                .isRecurring(Boolean.TRUE.equals(request.getIsRecurring()))
                .build();

        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public ExpenseResponse update(User currentUser, UUID expenseId, ExpenseRequest request) {
        Expense expense = getOwnedExpense(currentUser, expenseId);
        Category category = resolveCategory(currentUser, request.getCategoryId());

        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setMerchant(request.getMerchant());
        expense.setIsRecurring(Boolean.TRUE.equals(request.getIsRecurring()));

        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID expenseId) {
        Expense expense = getOwnedExpense(currentUser, expenseId);
        expenseRepository.delete(expense);
    }

    @Override
    public ExpenseResponse getById(User currentUser, UUID expenseId) {
        return expenseMapper.toResponse(getOwnedExpense(currentUser, expenseId));
    }

    @Override
    public PageResponse<ExpenseResponse> search(User currentUser, TransactionSearchRequest filter) {
        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.getSortBy());

        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Expense> page = expenseRepository.findAll(ExpenseSpecification.build(currentUser, filter), pageRequest);
        Page<ExpenseResponse> mapped = page.map(expenseMapper::toResponse);

        return PageResponse.from(mapped);
    }

    private Expense getOwnedExpense(User currentUser, UUID expenseId) {
        return expenseRepository.findByIdAndUser(expenseId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", expenseId));
    }

    private Category resolveCategory(User currentUser, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUser(categoryId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new BadRequestException("Selected category is not an expense category");
        }

        return category;
    }
}