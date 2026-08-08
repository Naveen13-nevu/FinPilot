package com.finpilot.repository;

import com.finpilot.entity.RecurringTransaction;
import com.finpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    Optional<RecurringTransaction> findByIdAndUser(UUID id, User user);

    List<RecurringTransaction> findByUserOrderByNextRunDateAsc(User user);

    List<RecurringTransaction> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}