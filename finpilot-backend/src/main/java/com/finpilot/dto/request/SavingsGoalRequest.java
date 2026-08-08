package com.finpilot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalRequest {

    @NotBlank(message = "Goal name is required")
    @Size(max = 150, message = "Goal name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    private BigDecimal targetAmount;

    @FutureOrPresent(message = "Target date must be today or in the future")
    private LocalDate targetDate;

    @Size(max = 50)
    private String icon;

    @Size(max = 20)
    private String color;
}