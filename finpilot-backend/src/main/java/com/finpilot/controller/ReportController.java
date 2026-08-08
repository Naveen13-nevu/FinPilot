package com.finpilot.controller;

import com.finpilot.entity.User;
import com.finpilot.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Export transaction reports as PDF or Excel")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pdf")
    @Operation(summary = "Download a PDF report of income/expense transactions for a period")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate resolvedStart = startDate != null ? startDate : YearMonth.now().atDay(1);
        LocalDate resolvedEnd = endDate != null ? endDate : YearMonth.now().atEndOfMonth();

        byte[] pdfBytes = reportService.generateTransactionsPdf(currentUser, resolvedStart, resolvedEnd);
        String filename = "finpilot-report-" + resolvedStart + "_to_" + resolvedEnd + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdfBytes);
    }

    @GetMapping("/excel")
    @Operation(summary = "Download an Excel report of income/expense transactions for a period")
    public ResponseEntity<byte[]> exportExcel(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate resolvedStart = startDate != null ? startDate : YearMonth.now().atDay(1);
        LocalDate resolvedEnd = endDate != null ? endDate : YearMonth.now().atEndOfMonth();

        byte[] excelBytes = reportService.generateTransactionsExcel(currentUser, resolvedStart, resolvedEnd);
        String filename = "finpilot-report-" + resolvedStart + "_to_" + resolvedEnd + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(excelBytes);
    }
}