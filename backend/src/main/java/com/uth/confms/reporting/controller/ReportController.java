package com.uth.confms.reporting.controller;

import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.reporting.dto.ConferenceStatsDTO;
import com.uth.confms.reporting.dto.ReviewStatsDTO;
import com.uth.confms.reporting.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
  private final ReportService reportService;

  public ReportController(ReportService reportService) {
    this.reportService = reportService;
  }

  @GetMapping("/conference/{conferenceId}/stats")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ConferenceStatsDTO>> getConferenceStats(
      @PathVariable Long conferenceId) {
    return ResponseEntity.ok(ApiResponse.success(reportService.getConferenceStats(conferenceId)));
  }

  @GetMapping("/conference/{conferenceId}/review-stats")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ReviewStatsDTO>> getReviewStats(
      @PathVariable Long conferenceId) {
    return ResponseEntity.ok(ApiResponse.success(reportService.getReviewStats(conferenceId)));
  }
}
