package com.finpilot.config;

import com.finpilot.service.EmiReminderService;
import com.finpilot.service.RecurringTransactionProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final RecurringTransactionProcessor recurringTransactionProcessor;
    private final EmiReminderService emiReminderService;

    /** Runs once every day at 1:00 AM server time to materialize due recurring transactions. */
    @Scheduled(cron = "0 0 1 * * *")
    public void runDailyRecurringTransactionJob() {
        recurringTransactionProcessor.processDueTransactions();
    }

    /** Runs once every day at 8:00 AM server time to remind users of upcoming EMI due dates. */
    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyEmiReminderJob() {
        emiReminderService.sendUpcomingEmiReminders();
    }
}