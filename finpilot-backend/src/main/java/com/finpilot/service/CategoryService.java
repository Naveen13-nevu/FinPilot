package com.finpilot.service;

import com.finpilot.dto.request.CategoryRequest;
import com.finpilot.dto.response.CategoryResponse;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.User;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse create(User currentUser, CategoryRequest request);

    CategoryResponse update(User currentUser, UUID categoryId, CategoryRequest request);

    void delete(User currentUser, UUID categoryId);

    CategoryResponse getById(User currentUser, UUID categoryId);

    List<CategoryResponse> getAll(User currentUser, CategoryType type);
}