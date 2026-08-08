package com.finpilot.service.impl;

import com.finpilot.dto.request.RecurringTransactionRequest;
import com.finpilot.dto.response.RecurringTransactionResponse;
import com.finpilot.entity.Category;
import com.finpilot.entity.RecurringTransaction;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.RecurringTransactionMapper;
import com.finpilot.repository.CategoryRepository;
import com.finpilot.repository.RecurringTransactionRepository;
import com.finpilot.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringTransactionMapper recurringTransactionMapper;

    @Override
    @Transactional
    public RecurringTransactionResponse create(User currentUser, RecurringTransactionRequest request) {
        Category category = resolveCategory(currentUser, request);

        RecurringTransaction recurring = RecurringTransaction.builder()
                .user(currentUser)
                .category(category)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextRunDate(request.getStartDate())
                .paymentMethod(request.getPaymentMethod())
                .active(true)
                .build();

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(User currentUser, UUID id, RecurringTransactionRequest request) {
        RecurringTransaction recurring = getOwned(currentUser, id);
        Category category = resolveCategory(currentUser, request);

        recurring.setCategory(category);
        recurring.setType(request.getType());
        recurring.setAmount(request.getAmount());
        recurring.setDescription(request.getDescription());
        recurring.setFrequency(request.getFrequency());
        recurring.setStartDate(request.getStartDate());
        recurring.setEndDate(request.getEndDate());
        recurring.setPaymentMethod(request.getPaymentMethod());

        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID id) {
        RecurringTransaction recurring = getOwned(currentUser, id);
        recurringTransactionRepository.delete(recurring);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse setActive(User currentUser, UUID id, boolean active) {
        RecurringTransaction recurring = getOwned(currentUser, id);
        recurring.setActive(active);
        return recurringTransactionMapper.toResponse(recurringTransactionRepository.save(recurring));
    }

    @Override
    public List<RecurringTransactionResponse> getAll(User currentUser) {
        return recurringTransactionRepository.findByUserOrderByNextRunDateAsc(currentUser)
                .stream()
                .map(recurringTransactionMapper::toResponse)
                .toList();
    }

    private Category resolveCategory(User currentUser, RecurringTransactionRequest request) {
        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (category.getType() != request.getType()) {
            throw new BadRequestException("Category type does not match the recurring transaction type");
        }

        return category;
    }

    private RecurringTransaction getOwned(User currentUser, UUID id) {
        return recurringTransactionRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction", "id", id));
    }
}