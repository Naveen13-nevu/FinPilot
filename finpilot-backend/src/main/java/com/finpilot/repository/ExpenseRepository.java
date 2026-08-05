package com.finpilot.repository;

import com.finpilot.entity.Expense;
import com.finpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndUser(UUID id, User user);

    List<Expense> findTop10ByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user " +
            "AND e.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndDateRange(@Param("user") User user,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('TO_CHAR', e.transactionDate, 'YYYY-MM'), COALESCE(SUM(e.amount), 0) " +
            "FROM Expense e WHERE e.user = :user AND e.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('TO_CHAR', e.transactionDate, 'YYYY-MM') " +
            "ORDER BY FUNCTION('TO_CHAR', e.transactionDate, 'YYYY-MM')")
    List<Object[]> sumGroupByMonth(@Param("user") User user,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category.id, e.category.name, e.category.color, COALESCE(SUM(e.amount), 0) " +
            "FROM Expense e WHERE e.user = :user AND e.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.category.id, e.category.name, e.category.color " +
            "ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumGroupByCategory(@Param("user") User user,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
}