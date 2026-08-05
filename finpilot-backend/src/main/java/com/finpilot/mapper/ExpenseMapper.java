package com.finpilot.mapper;

import com.finpilot.dto.response.ExpenseResponse;
import com.finpilot.entity.Expense;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ExpenseMapper {

    ExpenseResponse toResponse(Expense expense);
}