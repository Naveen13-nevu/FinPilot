package com.finpilot.service.impl;

import com.finpilot.entity.EmiPayment;
import com.finpilot.entity.Loan;
import com.finpilot.entity.LoanStatus;
import com.finpilot.entity.NotificationType;
import com.finpilot.repository.EmiPaymentRepository;
import com.finpilot.repository.LoanRepository;
import com.finpilot.service.EmiReminderService;
import com.finpilot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmiReminderServiceImpl implements EmiReminderService {

    private static final int REMINDER_WINDOW_DAYS = 3;

    private final LoanRepository loanRepository;
    private final EmiPaymentRepository emiPaymentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void sendUpcomingEmiReminders() {
        LocalDate today = LocalDate.now();

        for (Loan loan : loanRepository.findAll()) {
            if (loan.getStatus() != LoanStatus.ACTIVE) {
                continue;
            }

            int paidInstallments = emiPaymentRepository.countByLoan(loan);
            if (paidInstallments >= loan.getTenureMonths()) {
                continue;
            }

            LocalDate nextDueDate = loan.getStartDate().plusMonths(paidInstallments);
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, nextDueDate);

            if (daysUntilDue >= 0 && daysUntilDue <= REMINDER_WINDOW_DAYS) {
                notificationService.notify(
                        loan.getUser(),
                        NotificationType.EMI_DUE_REMINDER,
                        "EMI due soon: " + loan.getLoanName(),
                        "Your EMI of " + loan.getUser().getCurrency() + " " + loan.getEmiAmount()
                                + " for " + loan.getLoanName() + " is due on " + nextDueDate + ".",
                        loan.getId());
            }
        }
    }
}