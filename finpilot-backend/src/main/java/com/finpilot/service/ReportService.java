package com.finpilot.service;

import com.finpilot.entity.User;

import java.time.LocalDate;

public interface ReportService {

    byte[] generateTransactionsPdf(User currentUser, LocalDate startDate, LocalDate endDate);

    byte[] generateTransactionsExcel(User currentUser, LocalDate startDate, LocalDate endDate);
}