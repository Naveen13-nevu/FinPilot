package com.finpilot.controller;

import com.finpilot.dto.request.CategoryRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.CategoryResponse;
import com.finpilot.entity.CategoryType;
import com.finpilot.entity.User;
import com.finpilot.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Manage income and expense categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ApiResponse<CategoryResponse> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.success("Category updated successfully", categoryService.update(currentUser, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a category")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        categoryService.delete(currentUser, id);
        return ApiResponse.message("Category deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by id")
    public ApiResponse<CategoryResponse> getById(@AuthenticationPrincipal User currentUser, @PathVariable UUID id) {
        return ApiResponse.success(categoryService.getById(currentUser, id));
    }

    @GetMapping
    @Operation(summary = "List all active categories, optionally filtered by type")
    public ApiResponse<List<CategoryResponse>> getAll(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) CategoryType type) {
        return ApiResponse.success(categoryService.getAll(currentUser, type));
    }
}