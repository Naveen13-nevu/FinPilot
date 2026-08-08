package com.finpilot.repository;

import com.finpilot.entity.SavingsGoal;
import com.finpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

    Optional<SavingsGoal> findByIdAndUser(UUID id, User user);

    List<SavingsGoal> findByUserOrderByCreatedAtDesc(User user);
}