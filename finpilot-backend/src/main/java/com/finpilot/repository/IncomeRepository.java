package com.finpilot.repository;

import com.finpilot.entity.Income;
import com.finpilot.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepository extends JpaRepository<Income, UUID>, JpaSpecificationExecutor<Income> {

    Optional<Income> findByIdAndUser(UUID id, User user);

    List<Income> findTop10ByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user " +
            "AND i.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndDateRange(@Param("user") User user,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('TO_CHAR', i.transactionDate, 'YYYY-MM'), COALESCE(SUM(i.amount), 0) " +
            "FROM Income i WHERE i.user = :user AND i.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY FUNCTION('TO_CHAR', i.transactionDate, 'YYYY-MM') " +
            "ORDER BY FUNCTION('TO_CHAR', i.transactionDate, 'YYYY-MM')")
    List<Object[]> sumGroupByMonth(@Param("user") User user,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}