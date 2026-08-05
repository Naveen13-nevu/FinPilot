package com.finpilot.mapper;

import com.finpilot.dto.response.CategoryResponse;
import com.finpilot.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}