package com.finpilot.service.impl;

import com.finpilot.entity.Expense;
import com.finpilot.entity.Income;
import com.finpilot.entity.User;
import com.finpilot.repository.ExpenseRepository;
import com.finpilot.repository.IncomeRepository;
import com.finpilot.service.ReportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    private record TransactionRow(String type, String date, String category, String description, BigDecimal amount) {
    }

    @Override
    public byte[] generateTransactionsPdf(User currentUser, LocalDate startDate, LocalDate endDate) {
        List<TransactionRow> rows = collectRows(currentUser, startDate, endDate);
        BigDecimal totalIncome = incomeRepository.sumByUserAndDateRange(currentUser, startDate, endDate);
        BigDecimal totalExpense = expenseRepository.sumByUserAndDateRange(currentUser, startDate, endDate);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("FinPilot - Transaction Report")
                    .setBold().setFontSize(18));
            document.add(new Paragraph(startDate + "  to  " + endDate).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Total Income: " + currentUser.getCurrency() + " " + totalIncome)
                    .setFontColor(ColorConstants.GREEN).setBold());
            document.add(new Paragraph("Total Expense: " + currentUser.getCurrency() + " " + totalExpense)
                    .setFontColor(ColorConstants.RED).setBold());
            document.add(new Paragraph("Net: " + currentUser.getCurrency() + " " + totalIncome.subtract(totalExpense))
                    .setBold());
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{12, 12, 20, 36, 20}))
                    .useAllAvailableWidth();

            for (String header : new String[]{"Type", "Date", "Category", "Description", "Amount"}) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }

            for (TransactionRow row : rows) {
                table.addCell(row.type());
                table.addCell(row.date());
                table.addCell(row.category());
                table.addCell(row.description() != null ? row.description() : "-");
                table.addCell(new Cell().add(new Paragraph(row.amount().toString()))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public byte[] generateTransactionsExcel(User currentUser, LocalDate startDate, LocalDate endDate) {
        List<TransactionRow> rows = collectRows(currentUser, startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Type", "Date", "Category", "Description", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (TransactionRow row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.type());
                excelRow.createCell(1).setCellValue(row.date());
                excelRow.createCell(2).setCellValue(row.category());
                excelRow.createCell(3).setCellValue(row.description() != null ? row.description() : "-");
                excelRow.createCell(4, CellType.NUMERIC).setCellValue(row.amount().doubleValue());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private List<TransactionRow> collectRows(User currentUser, LocalDate startDate, LocalDate endDate) {
        List<TransactionRow> rows = new ArrayList<>();

        // Reuses the specification-backed repositories with a simple manual filter here
        // since reports need the full unpaginated range rather than a page.
        for (Income income : filterIncomesByRange(currentUser, startDate, endDate)) {
            rows.add(new TransactionRow("INCOME", income.getTransactionDate().toString(),
                    income.getCategory().getName(), income.getDescription(), income.getAmount()));
        }

        for (Expense expense : filterExpensesByRange(currentUser, startDate, endDate)) {
            rows.add(new TransactionRow("EXPENSE", expense.getTransactionDate().toString(),
                    expense.getCategory().getName(), expense.getDescription(), expense.getAmount()));
        }

        return rows.stream()
                .sorted(Comparator.comparing(TransactionRow::date))
                .toList();
    }

    private List<Income> filterIncomesByRange(User user, LocalDate start, LocalDate end) {
        return incomeRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("user"), user),
                cb.greaterThanOrEqualTo(root.get("transactionDate"), start),
                cb.lessThanOrEqualTo(root.get("transactionDate"), end)
        ));
    }

    private List<Expense> filterExpensesByRange(User user, LocalDate start, LocalDate end) {
        return expenseRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("user"), user),
                cb.greaterThanOrEqualTo(root.get("transactionDate"), start),
                cb.lessThanOrEqualTo(root.get("transactionDate"), end)
        ));
    }
}