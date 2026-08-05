package com.finpilot.mapper;

import com.finpilot.dto.response.IncomeResponse;
import com.finpilot.entity.Income;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface IncomeMapper {

    IncomeResponse toResponse(Income income);
}