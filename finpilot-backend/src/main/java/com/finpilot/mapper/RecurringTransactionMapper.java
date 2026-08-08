package com.finpilot.mapper;

import com.finpilot.dto.response.RecurringTransactionResponse;
import com.finpilot.entity.RecurringTransaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface RecurringTransactionMapper {

    RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction);
}