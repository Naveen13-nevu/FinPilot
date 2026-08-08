package com.finpilot.repository;

import com.finpilot.entity.Budget;
import com.finpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Optional<Budget> findByIdAndUser(UUID id, User user);

    List<Budget> findByUserAndBudgetMonthOrderByCategoryNameAsc(User user, LocalDate budgetMonth);

    boolean existsByUserAndCategoryIdAndBudgetMonth(User user, UUID categoryId, LocalDate budgetMonth);
}