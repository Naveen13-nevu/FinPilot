package com.finpilot.dto.response;

import com.finpilot.entity.LoanStatus;
import com.finpilot.entity.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private UUID id;
    private String loanName;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private LocalDate startDate;
    private BigDecimal outstandingPrincipal;
    private BigDecimal totalPaid;
    private Integer installmentsPaid;
    private Integer installmentsRemaining;
    private LoanStatus status;
    private String lender;
    private String notes;
}