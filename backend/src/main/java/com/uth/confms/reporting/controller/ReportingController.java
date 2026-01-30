package com.uth.confms.reporting.controller;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.reporting.dto.ReportResponseDTO;
import com.uth.confms.reporting.service.ReportingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý reports và statistics
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>GET /api/reporting/conference/{id} - Lấy latest report (CHAIR/ADMIN)
 * <li>POST /api/reporting/conference/{id}/snapshot - Tạo report snapshot
 * (CHAIR/ADMIN)
 * <li>GET /api/reporting/conference/{id}/history - Lấy report history
 * (CHAIR/ADMIN)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/reporting")
public class ReportingController {
  private final ReportingService reportingService;
  private final UserRepository userRepository;

  public ReportingController(ReportingService reportingService, UserRepository userRepository) {
    this.reportingService = reportingService;
    this.userRepository = userRepository;
  }

  @GetMapping("/conference/{conferenceId}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API lấy báo cáo tổng hợp mới nhất
  public ResponseEntity<ApiResponse<ReportResponseDTO>> getLatestReport(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(reportingService.getLatestReport(conferenceId, chairId)));
  }

  @PostMapping("/conference/{conferenceId}/snapshot")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API tạo snapshot báo cáo mới
  public ResponseEntity<ApiResponse<ReportResponseDTO>> createSnapshot(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(reportingService.createSnapshot(conferenceId, chairId)));
  }

  @GetMapping("/conference/{conferenceId}/history")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API xem lịch sử các báo cáo
  public ResponseEntity<ApiResponse<List<ReportResponseDTO>>> getReportHistory(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(reportingService.getReportHistory(conferenceId, chairId)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
  }
}
