package com.finpilot.service.impl;

import com.finpilot.dto.request.LoanRequest;
import com.finpilot.dto.response.EmiScheduleEntryResponse;
import com.finpilot.dto.response.LoanResponse;
import com.finpilot.entity.EmiPayment;
import com.finpilot.entity.Loan;
import com.finpilot.entity.LoanStatus;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.repository.EmiPaymentRepository;
import com.finpilot.repository.LoanRepository;
import com.finpilot.service.LoanService;
import com.finpilot.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final EmiPaymentRepository emiPaymentRepository;

    @Override
    @Transactional
    public LoanResponse create(User currentUser, LoanRequest request) {
        BigDecimal emiAmount = EmiCalculator.calculateEmi(
                request.getPrincipalAmount(), request.getInterestRate(), request.getTenureMonths());

        Loan loan = Loan.builder()
                .user(currentUser)
                .loanName(request.getLoanName())
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .interestRate(request.getInterestRate())
                .tenureMonths(request.getTenureMonths())
                .emiAmount(emiAmount)
                .startDate(request.getStartDate())
                .outstandingPrincipal(request.getPrincipalAmount())
                .status(LoanStatus.ACTIVE)
                .lender(request.getLender())
                .notes(request.getNotes())
                .build();

        return toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public LoanResponse update(User currentUser, UUID loanId, LoanRequest request) {
        Loan loan = getOwnedLoan(currentUser, loanId);

        if (emiPaymentRepository.countByLoan(loan) > 0) {
            throw new BadRequestException("Cannot modify loan terms after EMI payments have been recorded. Close this loan and create a new one instead.");
        }

        BigDecimal emiAmount = EmiCalculator.calculateEmi(
                request.getPrincipalAmount(), request.getInterestRate(), request.getTenureMonths());

        loan.setLoanName(request.getLoanName());
        loan.setLoanType(request.getLoanType());
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTenureMonths(request.getTenureMonths());
        loan.setEmiAmount(emiAmount);
        loan.setStartDate(request.getStartDate());
        loan.setOutstandingPrincipal(request.getPrincipalAmount());
        loan.setLender(request.getLender());
        loan.setNotes(request.getNotes());

        return toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public void delete(User currentUser, UUID loanId) {
        Loan loan = getOwnedLoan(currentUser, loanId);
        loanRepository.delete(loan);
    }

    @Override
    public LoanResponse getById(User currentUser, UUID loanId) {
        return toResponse(getOwnedLoan(currentUser, loanId));
    }

    @Override
    public List<LoanResponse> getAll(User currentUser) {
        return loanRepository.findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<EmiScheduleEntryResponse> getAmortizationSchedule(User currentUser, UUID loanId) {
        Loan loan = getOwnedLoan(currentUser, loanId);
        int paidInstallments = emiPaymentRepository.countByLoan(loan);

        BigDecimal monthlyRate = EmiCalculator.monthlyRate(loan.getInterestRate());
        BigDecimal balance = loan.getPrincipalAmount();

        List<EmiScheduleEntryResponse> schedule = new ArrayList<>();

        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            BigDecimal interestComponent = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalComponent = loan.getEmiAmount().subtract(interestComponent);

            // Last installment absorbs rounding difference
            if (i == loan.getTenureMonths()) {
                principalComponent = balance;
            }

            balance = balance.subtract(principalComponent).max(BigDecimal.ZERO);

            schedule.add(EmiScheduleEntryResponse.builder()
                    .installmentNumber(i)
                    .dueDate(loan.getStartDate().plusMonths(i - 1L))
                    .emiAmount(loan.getEmiAmount())
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .outstandingBalance(balance)
                    .paid(i <= paidInstallments)
                    .build());
        }

        return schedule;
    }

    @Override
    @Transactional
    public LoanResponse recordEmiPayment(User currentUser, UUID loanId) {
        Loan loan = getOwnedLoan(currentUser, loanId);

        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new BadRequestException("This loan is already closed");
        }

        int nextInstallment = emiPaymentRepository.countByLoan(loan) + 1;
        if (nextInstallment > loan.getTenureMonths()) {
            throw new BadRequestException("All installments for this loan have already been paid");
        }

        List<EmiScheduleEntryResponse> schedule = getAmortizationSchedule(currentUser, loanId);
        EmiScheduleEntryResponse entry = schedule.get(nextInstallment - 1);

        EmiPayment payment = EmiPayment.builder()
                .loan(loan)
                .installmentNumber(nextInstallment)
                .paymentDate(LocalDate.now())
                .emiAmount(entry.getEmiAmount())
                .principalComponent(entry.getPrincipalComponent())
                .interestComponent(entry.getInterestComponent())
                .outstandingBalanceAfter(entry.getOutstandingBalance())
                .build();

        emiPaymentRepository.save(payment);

        loan.setOutstandingPrincipal(entry.getOutstandingBalance());
        if (nextInstallment == loan.getTenureMonths()) {
            loan.setStatus(LoanStatus.CLOSED);
        }

        return toResponse(loanRepository.save(loan));
    }

    private LoanResponse toResponse(Loan loan) {
        int paidInstallments = emiPaymentRepository.countByLoan(loan);
        BigDecimal totalPaid = loan.getEmiAmount().multiply(BigDecimal.valueOf(paidInstallments));

        return LoanResponse.builder()
                .id(loan.getId())
                .loanName(loan.getLoanName())
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .startDate(loan.getStartDate())
                .outstandingPrincipal(loan.getOutstandingPrincipal())
                .totalPaid(totalPaid)
                .installmentsPaid(paidInstallments)
                .installmentsRemaining(loan.getTenureMonths() - paidInstallments)
                .status(loan.getStatus())
                .lender(loan.getLender())
                .notes(loan.getNotes())
                .build();
    }

    private Loan getOwnedLoan(User currentUser, UUID loanId) {
        return loanRepository.findByIdAndUser(loanId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId));
    }
}