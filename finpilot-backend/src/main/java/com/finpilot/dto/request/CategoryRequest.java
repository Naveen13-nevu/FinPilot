package com.finpilot.dto.request;

import com.finpilot.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    @Size(max = 50, message = "Icon identifier must not exceed 50 characters")
    private String icon;

    @Size(max = 20, message = "Color must not exceed 20 characters")
    private String color;
}