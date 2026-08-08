package com.finpilot.mapper;

import com.finpilot.dto.response.SavingsGoalResponse;
import com.finpilot.entity.SavingsGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SavingsGoalMapper {

    @Mapping(target = "progressPercent", expression = "java(goal.getProgressPercent())")
    SavingsGoalResponse toResponse(SavingsGoal goal);
}