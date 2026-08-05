package com.finpilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdownResponse {

    private UUID categoryId;
    private String categoryName;
    private String color;
    private BigDecimal amount;
    private BigDecimal percentage;
}