package com.finpilot.repository;

import com.finpilot.entity.EmiPayment;
import com.finpilot.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmiPaymentRepository extends JpaRepository<EmiPayment, UUID> {

    List<EmiPayment> findByLoanOrderByInstallmentNumberAsc(Loan loan);

    int countByLoan(Loan loan);
}