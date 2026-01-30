package com.uth.confms.reporting.controller;

import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.reporting.dto.ConferenceStatsDTO;
import com.uth.confms.reporting.dto.ReviewStatsDTO;
import com.uth.confms.reporting.dto.SLAStatsDTO;
import com.uth.confms.reporting.service.ReportExportService;
import com.uth.confms.reporting.service.ReportService;
import com.uth.confms.reporting.service.SLAService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
  private final ReportService reportService;
  private final ReportExportService reportExportService;
  private final SLAService slaService;

  @GetMapping("/conference/{conferenceId}/stats")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API lấy thống kê chung của conference
  public ResponseEntity<ApiResponse<ConferenceStatsDTO>> getConferenceStats(
      @PathVariable Long conferenceId) {
    return ResponseEntity.ok(ApiResponse.success(reportService.getConferenceStats(conferenceId)));
  }

  @GetMapping("/conference/{conferenceId}/review-stats")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API lấy thống kê chi tiết về review
  public ResponseEntity<ApiResponse<ReviewStatsDTO>> getReviewStats(
      @PathVariable Long conferenceId) {
    return ResponseEntity.ok(ApiResponse.success(reportService.getReviewStats(conferenceId)));
  }

  @GetMapping("/conference/{conferenceId}/sla-stats")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API lấy thống kê SLA
  public ResponseEntity<ApiResponse<SLAStatsDTO>> getSLAStats(
      @PathVariable Long conferenceId) {
    return ResponseEntity.ok(ApiResponse.success(slaService.getSLAStats(conferenceId)));
  }

  @GetMapping("/export")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API xuất báo cáo (PDF, Excel, CSV)
  public ResponseEntity<byte[]> exportReport(
      @RequestParam Long conferenceId,
      @RequestParam(defaultValue = "ALL") String reportType,
      @RequestParam(defaultValue = "CSV") String format) {

    byte[] exportData;
    String filename;
    String contentType;

    if ("PDF".equalsIgnoreCase(format)) {
      exportData = reportExportService.exportToPdf(conferenceId, reportType);
      filename = String.format("report_%s_%s.pdf", conferenceId, LocalDate.now());
      contentType = "application/pdf";
    } else if ("EXCEL".equalsIgnoreCase(format) || "XLSX".equalsIgnoreCase(format)) {
      exportData = reportExportService.exportToExcel(conferenceId, reportType);
      filename = String.format("report_%s_%s.xlsx", conferenceId, LocalDate.now());
      contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    } else {
      // Default to CSV
      exportData = reportExportService.exportToCsv(conferenceId, reportType);
      filename = String.format("report_%s_%s.csv", conferenceId, LocalDate.now());
      contentType = "text/csv";
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(exportData);
  }
}
