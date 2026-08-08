package com.finpilot.service;

public interface RecurringTransactionProcessor {

    /**
     * Finds all active recurring transactions whose nextRunDate has arrived,
     * creates the corresponding Income/Expense record, and advances nextRunDate.
     */
    void processDueTransactions();
}