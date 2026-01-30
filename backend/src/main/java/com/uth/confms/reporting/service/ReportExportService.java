package com.uth.confms.reporting.service;

import com.uth.confms.decision.entity.Decision;
import com.uth.confms.decision.repository.DecisionRepository;
import com.uth.confms.review.entity.Review;
import java.util.concurrent.atomic.AtomicInteger;
import com.uth.confms.review.repository.ReviewRepository;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import com.uth.confms.common.util.PdfFontUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service để export reports sang các formats khác nhau (CSV, PDF)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final SubmissionRepository submissionRepository;
    private final ReviewRepository reviewRepository;
    private final DecisionRepository decisionRepository;

    /**
     * Export report sang CSV
     */
    // Xuất báo cáo ra định dạng CSV
    public byte[] exportToCsv(Long conferenceId, String reportType) {
        log.info("Exporting report to CSV: conferenceId={}, reportType={}", conferenceId, reportType);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {

            if ("STATISTICS".equals(reportType) || "ALL".equals(reportType)) {
                exportStatisticsCsv(writer, conferenceId);
            }

            if ("SUBMISSIONS".equals(reportType) || "ALL".equals(reportType)) {
                exportSubmissionsCsv(writer, conferenceId);
            }

            if ("REVIEWS".equals(reportType) || "ALL".equals(reportType)) {
                exportReviewsCsv(writer, conferenceId);
            }

            if ("DECISIONS".equals(reportType) || "ALL".equals(reportType)) {
                exportDecisionsCsv(writer, conferenceId);
            }

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting report to CSV", e);
            throw new com.uth.confms.common.exception.BusinessException("Failed to export report: " + e.getMessage());
        }
    }

    /**
     * Export report sang Excel
     */
    // Xuất báo cáo ra định dạng Excel
    public byte[] exportToExcel(Long conferenceId, String reportType) {
        log.info("Exporting report to Excel: conferenceId={}, reportType={}", conferenceId, reportType);

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            if ("STATISTICS".equals(reportType) || "ALL".equals(reportType)) {
                addStatisticsSheet(workbook, conferenceId, headerStyle, dataStyle);
            }

            if ("SUBMISSIONS".equals(reportType) || "ALL".equals(reportType)) {
                addSubmissionsSheet(workbook, conferenceId, headerStyle, dataStyle);
            }

            if ("REVIEWS".equals(reportType) || "ALL".equals(reportType)) {
                addReviewsSheet(workbook, conferenceId, headerStyle, dataStyle);
            }

            if ("DECISIONS".equals(reportType) || "ALL".equals(reportType)) {
                addDecisionsSheet(workbook, conferenceId, headerStyle, dataStyle);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting report to Excel", e);
            throw new com.uth.confms.common.exception.BusinessException("Failed to export report: " + e.getMessage());
        }
    }

    /**
     * Export report sang PDF
     */
    // Xuất báo cáo ra định dạng PDF
    public byte[] exportToPdf(Long conferenceId, String reportType) {
        log.info("Exporting report to PDF: conferenceId={}, reportType={}", conferenceId, reportType);

        try (PDDocument document = new PDDocument()) {
            int currentPage = 1;

            if ("STATISTICS".equals(reportType) || "ALL".equals(reportType)) {
                currentPage = addStatisticsPage(document, conferenceId, currentPage);
            }

            if ("SUBMISSIONS".equals(reportType) || "ALL".equals(reportType)) {
                currentPage = addSubmissionsPage(document, conferenceId, currentPage);
            }

            if ("REVIEWS".equals(reportType) || "ALL".equals(reportType)) {
                currentPage = addReviewsPage(document, conferenceId, currentPage);
            }

            if ("DECISIONS".equals(reportType) || "ALL".equals(reportType)) {
                currentPage = addDecisionsPage(document, conferenceId, currentPage);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting report to PDF", e);
            throw new com.uth.confms.common.exception.BusinessException("Failed to export report: " + e.getMessage());
        }
    }

    private void exportStatisticsCsv(PrintWriter writer, Long conferenceId) {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

        long accepted = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.ACCEPTED)
                .count();
        long rejected = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.REJECTED)
                .count();
        long pending = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
                        || s.getStatus() == Submission.SubmissionStatus.SUBMITTED
                        || s.getStatus() == Submission.SubmissionStatus.REVIEWED)
                .count();
        double acceptanceRate = submissions.size() > 0 ? (double) accepted / submissions.size() * 100 : 0.0;

        writer.println("=== STATISTICS ===");
        writer.println("total_submissions,accepted,rejected,pending,acceptance_rate");
        writer.printf("%d,%d,%d,%d,%.2f%n", submissions.size(), accepted, rejected, pending, acceptanceRate);
        writer.println();
    }

    private void exportSubmissionsCsv(PrintWriter writer, Long conferenceId) {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

        writer.println("=== SUBMISSIONS ===");
        writer.println("id,title,status,author_id,track_id,created_at");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Submission submission : submissions) {
            writer.printf("%d,\"%s\",%s,%d,%s,\"%s\"%n",
                    submission.getId(),
                    escapeCsv(submission.getTitle()),
                    submission.getStatus(),
                    submission.getAuthorId(),
                    submission.getTrackId() != null ? submission.getTrackId() : "",
                    submission.getCreatedAt() != null ? submission.getCreatedAt().format(formatter) : "");
        }
        writer.println();
    }

    private void exportReviewsCsv(PrintWriter writer, Long conferenceId) {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        writer.println("=== REVIEWS ===");
        writer.println("review_id,submission_id,reviewer_id,status,score,created_at,submitted_at");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Long submissionId : submissionIds) {
            List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);
            for (Review review : reviews) {
                writer.printf("%d,%d,%d,%s,%s,\"%s\",\"%s\"%n",
                        review.getId(),
                        submissionId,
                        review.getReviewerId(),
                        review.getStatus(),
                        review.getScore() != null ? review.getScore().toString() : "",
                        review.getCreatedAt() != null ? review.getCreatedAt().format(formatter) : "",
                        review.getSubmittedAt() != null ? review.getSubmittedAt().format(formatter) : "");
            }
        }
        writer.println();
    }

    private void exportDecisionsCsv(PrintWriter writer, Long conferenceId) {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        writer.println("=== DECISIONS ===");
        writer.println("decision_id,submission_id,type,decided_by,decided_at,notified,locked");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Long submissionId : submissionIds) {
            decisionRepository.findBySubmissionId(submissionId).ifPresent(decision -> {
                writer.printf("%d,%d,%s,%d,\"%s\",%s,%s%n",
                        decision.getId(),
                        submissionId,
                        decision.getType(),
                        decision.getDecidedBy(),
                        decision.getDecidedAt() != null ? decision.getDecidedAt().format(formatter) : "",
                        decision.getNotified() != null ? decision.getNotified() : false,
                        decision.getLocked() != null ? decision.getLocked() : false);
            });
        }
        writer.println();
    }

    private int addStatisticsPage(PDDocument document, Long conferenceId, int startPage) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        long accepted = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.ACCEPTED)
                .count();
        long rejected = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.REJECTED)
                .count();
        long pending = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
                        || s.getStatus() == Submission.SubmissionStatus.SUBMITTED
                        || s.getStatus() == Submission.SubmissionStatus.REVIEWED)
                .count();
        double acceptanceRate = submissions.size() > 0 ? (double) accepted / submissions.size() * 100 : 0.0;

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(boldFont, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Conference Statistics Report");
            contentStream.endText();

            int yPos = 700;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Total Submissions: " + submissions.size());
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Accepted: " + accepted);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Rejected: " + rejected);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Pending: " + pending);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText(String.format("Acceptance Rate: %.2f%%", acceptanceRate));
            contentStream.endText();
        }

        return startPage + 1;
    }

    private int addSubmissionsPage(PDDocument document, Long conferenceId, int startPage) throws Exception {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        int itemsPerPage = 30;
        int totalPages = (submissions.size() + itemsPerPage - 1) / itemsPerPage;

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);
        boolean hasUnicodeFont = PdfFontUtil.hasCustomFonts();

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(boldFont, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Submissions List");
                contentStream.endText();

                int startIdx = pageNum * itemsPerPage;
                int endIdx = Math.min(startIdx + itemsPerPage, submissions.size());
                int yPos = 720;

                for (int i = startIdx; i < endIdx; i++) {
                    Submission submission = submissions.get(i);
                    String title = submission.getTitle() != null ? submission.getTitle() : "Untitled";
                    if (title.length() > 60) {
                        title = title.substring(0, 57) + "...";
                    }

                    contentStream.beginText();
                    contentStream.setFont(regularFont, 10);
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText(String.format("%d. %s [%s]", i + 1,
                            PdfFontUtil.prepareText(title, hasUnicodeFont), submission.getStatus()));
                    contentStream.endText();

                    yPos -= 15;
                }
            }
            startPage++;
        }

        return startPage;
    }

    private int addReviewsPage(PDDocument document, Long conferenceId, int startPage) throws Exception {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(boldFont, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Reviews Summary");
            contentStream.endText();

            int totalReviews = 0;
            int completedReviews = 0;

            for (Long submissionId : submissionIds) {
                List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);
                totalReviews += reviews.size();
                completedReviews += (int) reviews.stream()
                        .filter(r -> r.getStatus() == Review.ReviewStatus.SUBMITTED)
                        .count();
            }

            int yPos = 700;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Total Reviews: " + totalReviews);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Completed Reviews: " + completedReviews);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Pending Reviews: " + (totalReviews - completedReviews));
            contentStream.endText();
        }

        return startPage + 1;
    }

    private int addDecisionsPage(PDDocument document, Long conferenceId, int startPage) throws Exception {
        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        // Load Unicode-compatible fonts for Vietnamese support
        PDFont boldFont = PdfFontUtil.loadBoldFont(document);
        PDFont regularFont = PdfFontUtil.loadRegularFont(document);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(boldFont, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Decisions Summary");
            contentStream.endText();

            AtomicInteger totalDecisions = new AtomicInteger(0);
            AtomicInteger accepted = new AtomicInteger(0);
            AtomicInteger rejected = new AtomicInteger(0);

            for (Long submissionId : submissionIds) {
                decisionRepository.findBySubmissionId(submissionId).ifPresent(decision -> {
                    totalDecisions.incrementAndGet();
                    if (decision.getType() == Decision.DecisionType.ACCEPT
                            || decision.getType() == Decision.DecisionType.CONDITIONAL_ACCEPT) {
                        accepted.incrementAndGet();
                    } else if (decision.getType() == Decision.DecisionType.REJECT) {
                        rejected.incrementAndGet();
                    }
                });
            }

            int totalDecisionsInt = totalDecisions.get();
            int acceptedInt = accepted.get();
            int rejectedInt = rejected.get();

            int yPos = 700;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Total Decisions: " + totalDecisionsInt);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Accepted: " + acceptedInt);
            contentStream.endText();

            yPos -= 20;
            contentStream.beginText();
            contentStream.setFont(regularFont, 12);
            contentStream.newLineAtOffset(50, yPos);
            contentStream.showText("Rejected: " + rejectedInt);
            contentStream.endText();
        }

        return startPage + 1;
    }

    private String escapeCsv(String value) {
        if (value == null)
            return "";
        return value.replace("\"", "\"\"");
    }

    private void addStatisticsSheet(Workbook workbook, Long conferenceId, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Statistics");

        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        long accepted = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.ACCEPTED)
                .count();
        long rejected = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.REJECTED)
                .count();
        long pending = submissions.stream()
                .filter(s -> s.getStatus() == Submission.SubmissionStatus.UNDER_REVIEW
                        || s.getStatus() == Submission.SubmissionStatus.SUBMITTED
                        || s.getStatus() == Submission.SubmissionStatus.REVIEWED)
                .count();
        double acceptanceRate = submissions.size() > 0 ? (double) accepted / submissions.size() * 100 : 0.0;

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Metric");
        headerCell.setCellStyle(headerStyle);
        headerCell = headerRow.createCell(1);
        headerCell.setCellValue("Value");
        headerCell.setCellStyle(headerStyle);

        addDataRow(sheet, rowNum++, "Total Submissions", submissions.size(), dataStyle);
        addDataRow(sheet, rowNum++, "Accepted", accepted, dataStyle);
        addDataRow(sheet, rowNum++, "Rejected", rejected, dataStyle);
        addDataRow(sheet, rowNum++, "Pending", pending, dataStyle);
        addDataRow(sheet, rowNum++, "Acceptance Rate (%)", acceptanceRate, dataStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void addSubmissionsSheet(Workbook workbook, Long conferenceId, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Submissions");

        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = { "ID", "Title", "Status", "Author ID", "Track ID", "Created At" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Submission submission : submissions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(submission.getId());
            row.createCell(1).setCellValue(submission.getTitle() != null ? submission.getTitle() : "");
            row.createCell(2).setCellValue(submission.getStatus() != null ? submission.getStatus().toString() : "");
            row.createCell(3).setCellValue(submission.getAuthorId());
            if (submission.getTrackId() != null) {
                row.createCell(4).setCellValue(submission.getTrackId());
            } else {
                row.createCell(4).setCellValue("");
            }
            row.createCell(5)
                    .setCellValue(submission.getCreatedAt() != null ? submission.getCreatedAt().format(formatter) : "");

            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addReviewsSheet(Workbook workbook, Long conferenceId, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Reviews");

        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = { "Review ID", "Submission ID", "Reviewer ID", "Status", "Score", "Created At",
                "Submitted At" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Long submissionId : submissionIds) {
            List<Review> reviews = reviewRepository.findBySubmissionId(submissionId);
            for (Review review : reviews) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(review.getId());
                row.createCell(1).setCellValue(submissionId);
                row.createCell(2).setCellValue(review.getReviewerId());
                row.createCell(3).setCellValue(review.getStatus() != null ? review.getStatus().toString() : "");
                row.createCell(4).setCellValue(review.getScore() != null ? review.getScore().toString() : "");
                row.createCell(5)
                        .setCellValue(review.getCreatedAt() != null ? review.getCreatedAt().format(formatter) : "");
                row.createCell(6)
                        .setCellValue(review.getSubmittedAt() != null ? review.getSubmittedAt().format(formatter) : "");

                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addDecisionsSheet(Workbook workbook, Long conferenceId, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Decisions");

        List<Submission> submissions = submissionRepository.findByConferenceId(conferenceId);
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = { "Decision ID", "Submission ID", "Type", "Decided By", "Decided At", "Notified", "Locked" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Long submissionId : submissionIds) {
            final int currentRow = rowNum;
            decisionRepository.findBySubmissionId(submissionId).ifPresent(decision -> {
                Row row = sheet.createRow(currentRow);
                row.createCell(0).setCellValue(decision.getId());
                row.createCell(1).setCellValue(submissionId);
                row.createCell(2).setCellValue(decision.getType() != null ? decision.getType().toString() : "");
                row.createCell(3).setCellValue(decision.getDecidedBy());
                row.createCell(4)
                        .setCellValue(decision.getDecidedAt() != null ? decision.getDecidedAt().format(formatter) : "");
                row.createCell(5).setCellValue(decision.getNotified() != null ? decision.getNotified() : false);
                row.createCell(6).setCellValue(decision.getLocked() != null ? decision.getLocked() : false);

                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            });
            if (decisionRepository.findBySubmissionId(submissionId).isPresent()) {
                rowNum++;
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void addDataRow(Sheet sheet, int rowNum, String label, Object value, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(dataStyle);

        Cell valueCell = row.createCell(1);
        if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else {
            valueCell.setCellValue(value != null ? value.toString() : "");
        }
        valueCell.setCellStyle(dataStyle);
    }
}
