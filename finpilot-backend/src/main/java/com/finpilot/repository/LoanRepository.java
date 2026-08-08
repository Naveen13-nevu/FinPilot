package com.finpilot.repository;

import com.finpilot.entity.Loan;
import com.finpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    Optional<Loan> findByIdAndUser(UUID id, User user);

    List<Loan> findByUserOrderByCreatedAtDesc(User user);
}