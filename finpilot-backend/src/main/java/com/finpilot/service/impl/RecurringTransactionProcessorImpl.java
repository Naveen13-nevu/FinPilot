package com.finpilot.service.impl;

import com.finpilot.entity.Expense;
import com.finpilot.entity.Income;
import com.finpilot.entity.RecurringTransaction;
import com.finpilot.repository.ExpenseRepository;
import com.finpilot.repository.IncomeRepository;
import com.finpilot.repository.RecurringTransactionRepository;
import com.finpilot.service.RecurringTransactionProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionProcessorImpl implements RecurringTransactionProcessor {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional
    public void processDueTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringTransactionRepository.findByActiveTrueAndNextRunDateLessThanEqual(today);

        for (RecurringTransaction recurring : due) {
            materialize(recurring);
            advanceOrDeactivate(recurring, today);
        }

        if (!due.isEmpty()) {
            log.info("Processed {} due recurring transaction(s)", due.size());
        }
    }

    private void materialize(RecurringTransaction recurring) {
        switch (recurring.getType()) {
            case INCOME -> incomeRepository.save(Income.builder()
                    .user(recurring.getUser())
                    .category(recurring.getCategory())
                    .amount(recurring.getAmount())
                    .description(recurring.getDescription())
                    .transactionDate(recurring.getNextRunDate())
                    .paymentMethod(recurring.getPaymentMethod())
                    .source("Recurring")
                    .isRecurring(true)
                    .build());
            case EXPENSE -> expenseRepository.save(Expense.builder()
                    .user(recurring.getUser())
                    .category(recurring.getCategory())
                    .amount(recurring.getAmount())
                    .description(recurring.getDescription())
                    .transactionDate(recurring.getNextRunDate())
                    .paymentMethod(recurring.getPaymentMethod())
                    .merchant("Recurring")
                    .isRecurring(true)
                    .build());
        }
    }

    private void advanceOrDeactivate(RecurringTransaction recurring, LocalDate today) {
        LocalDate next = switch (recurring.getFrequency()) {
            case DAILY -> recurring.getNextRunDate().plusDays(1);
            case WEEKLY -> recurring.getNextRunDate().plusWeeks(1);
            case MONTHLY -> recurring.getNextRunDate().plusMonths(1);
            case YEARLY -> recurring.getNextRunDate().plusYears(1);
        };

        if (recurring.getEndDate() != null && next.isAfter(recurring.getEndDate())) {
            recurring.setActive(false);
        } else {
            recurring.setNextRunDate(next);
        }

        recurringTransactionRepository.save(recurring);
    }
}