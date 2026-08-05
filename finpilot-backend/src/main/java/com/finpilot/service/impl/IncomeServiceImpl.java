package com.finpilot.service.impl;

import com.finpilot.dto.request.IncomeRequest;
import com.finpilot.dto.request.TransactionSearchRequest;
import com.finpilot.dto.response.IncomeResponse;
import com.finpilot.dto.response.PageResponse;
import com.finpilot.entity.Category;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.Income;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.IncomeMapper;
import com.finpilot.repository.CategoryRepository;
import com.finpilot.repository.IncomeRepository;
import com.finpilot.repository.spec.IncomeSpecification;
import com.finpilot.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final IncomeMapper incomeMapper;

    @Override
    @Transactional
    public IncomeResponse create(User currentUser, IncomeRequest request) {
        Category category = resolveCategory(currentUser, request.getCategoryId());

        Income income = Income.builder()
                .user(currentUser)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .paymentMethod(request.getPaymentMethod())
                .source(request.getSource())
                .isRecurring(Boolean.TRUE.equals(request.getIsRecurring()))
                .build();

        return incomeMapper.toResponse(incomeRepository.save(income));
    }

    @Override
    @Transactional
    public IncomeResponse update(User currentUser, UUID incomeId, IncomeRequest request) {
        Income income = getOwnedIncome(currentUser, incomeId);
        Category category = resolveCategory(currentUser, request.getCategoryId());

        income.setCategory(category);
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setTransactionDate(request.getTransactionDate());
        income.setPaymentMethod(request.getPaymentMethod());
        income.setSource(request.getSource());
        income.setIsRecurring(Boolean.TRUE.equals(request.getIsRecurring()));

        return incomeMapper.toResponse(incomeRepository.save(income));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID incomeId) {
        Income income = getOwnedIncome(currentUser, incomeId);
        incomeRepository.delete(income);
    }

    @Override
    public IncomeResponse getById(User currentUser, UUID incomeId) {
        return incomeMapper.toResponse(getOwnedIncome(currentUser, incomeId));
    }

    @Override
    public PageResponse<IncomeResponse> search(User currentUser, TransactionSearchRequest filter) {
        Sort sort = Sort.by(
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                filter.getSortBy());

        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Income> page = incomeRepository.findAll(IncomeSpecification.build(currentUser, filter), pageRequest);
        Page<IncomeResponse> mapped = page.map(incomeMapper::toResponse);

        return PageResponse.from(mapped);
    }

    private Income getOwnedIncome(User currentUser, UUID incomeId) {
        return incomeRepository.findByIdAndUser(incomeId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Income", "id", incomeId));
    }

    private Category resolveCategory(User currentUser, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUser(categoryId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (category.getType() != CategoryType.INCOME) {
            throw new BadRequestException("Selected category is not an income category");
        }

        return category;
    }
}