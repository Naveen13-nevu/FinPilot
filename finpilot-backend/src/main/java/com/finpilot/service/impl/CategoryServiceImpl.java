package com.finpilot.service.impl;

import com.finpilot.dto.request.CategoryRequest;
import com.finpilot.dto.response.CategoryResponse;
import com.finpilot.entity.Category;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.DuplicateResourceException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.CategoryMapper;
import com.finpilot.repository.CategoryRepository;
import com.finpilot.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(User currentUser, CategoryRequest request) {
        if (categoryRepository.existsByUserAndNameIgnoreCaseAndType(currentUser, request.getName(), request.getType())) {
            throw new DuplicateResourceException(
                    "A " + request.getType().name().toLowerCase() + " category named '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .user(currentUser)
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .color(request.getColor())
                .isDefault(false)
                .active(true)
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(User currentUser, UUID categoryId, CategoryRequest request) {
        Category category = getOwnedCategory(currentUser, categoryId);

        if (category.getIsDefault()) {
            throw new BadRequestException("Default categories cannot be modified");
        }

        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID categoryId) {
        Category category = getOwnedCategory(currentUser, categoryId);

        if (category.getIsDefault()) {
            throw new BadRequestException("Default categories cannot be deleted");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    public CategoryResponse getById(User currentUser, UUID categoryId) {
        return categoryMapper.toResponse(getOwnedCategory(currentUser, categoryId));
    }

    @Override
    public List<CategoryResponse> getAll(User currentUser, CategoryType type) {
        List<Category> categories = (type != null)
                ? categoryRepository.findByUserAndTypeAndActiveTrueOrderByNameAsc(currentUser, type)
                : categoryRepository.findByUserAndActiveTrueOrderByNameAsc(currentUser);

        return categories.stream().map(categoryMapper::toResponse).toList();
    }

    private Category getOwnedCategory(User currentUser, UUID categoryId) {
        return categoryRepository.findByIdAndUser(categoryId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
    }
}