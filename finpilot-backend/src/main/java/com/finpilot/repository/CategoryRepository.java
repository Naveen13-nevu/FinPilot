package com.finpilot.repository;

import com.finpilot.entity.Category;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUserAndActiveTrueOrderByNameAsc(User user);

    List<Category> findByUserAndTypeAndActiveTrueOrderByNameAsc(User user, CategoryType type);

    Optional<Category> findByIdAndUser(UUID id, User user);

    boolean existsByUserAndNameIgnoreCaseAndType(User user, String name, CategoryType type);
}