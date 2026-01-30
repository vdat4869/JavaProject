package com.uth.confms.decision.controller;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.decision.dto.BulkNotificationRequestDTO;
import com.uth.confms.decision.dto.BulkDecisionRequestDTO;
import com.uth.confms.decision.dto.DecisionHistoryDTO;
import com.uth.confms.decision.dto.DecisionRequestDTO;
import com.uth.confms.decision.dto.DecisionResultDTO;
import com.uth.confms.decision.service.DecisionService;
import com.uth.confms.decision.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý decisions và notifications
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>POST /api/decisions - Tạo decision (CHAIR/ADMIN)
 * <li>GET /api/decisions/submission/{id} - Lấy decision by submission
 * (authenticated)
 * <li>GET /api/decisions/conference/{id} - Lấy decisions của conference
 * (CHAIR/ADMIN)
 * <li>GET /api/decisions/pending-notifications - Lấy pending notifications
 * (CHAIR/ADMIN)
 * <li>POST /api/decisions/notify/{id} - Gửi notification cho decision
 * (CHAIR/ADMIN)
 * <li>POST /api/decisions/notifications/bulk - Gửi bulk notifications
 * (CHAIR/ADMIN)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/decisions")
public class DecisionController {
  private final DecisionService decisionService;
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  public DecisionController(
      DecisionService decisionService,
      NotificationService notificationService,
      UserRepository userRepository) {
    this.decisionService = decisionService;
    this.notificationService = notificationService;
    this.userRepository = userRepository;
  }

  @PostMapping
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Tạo decision mới cho submission (Chair/Admin)
  public ResponseEntity<ApiResponse<DecisionResultDTO>> makeDecision(
      @Valid @RequestBody DecisionRequestDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(ApiResponse.success(decisionService.makeDecision(dto, chairId, isAdmin)));
  }

  @PostMapping("/bulk")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Tạo decision hàng loạt (Chair/Admin)
  public ResponseEntity<ApiResponse<List<DecisionResultDTO>>> makeBulkDecisions(
      @Valid @RequestBody BulkDecisionRequestDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(ApiResponse.success(decisionService.makeBulkDecisions(dto, chairId, isAdmin)));
  }

  @GetMapping("/{id}/history")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Xem lịch sử thay đổi decision
  public ResponseEntity<ApiResponse<List<DecisionHistoryDTO>>> getDecisionHistory(
      @PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(decisionService.getDecisionHistoryDTOs(id)));
  }

  @GetMapping("/submission/{submissionId}")
  @PreAuthorize("isAuthenticated()")
  // Lấy decision của submission
  public ResponseEntity<ApiResponse<DecisionResultDTO>> getDecisionBySubmission(
      @PathVariable Long submissionId) {
    return ResponseEntity.ok(
        ApiResponse.success(decisionService.getDecisionBySubmission(submissionId)));
  }

  @GetMapping("/conference/{conferenceId}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Lấy danh sách decision theo hội nghị (Chair/Admin)
  public ResponseEntity<ApiResponse<List<DecisionResultDTO>>> getDecisionsByConference(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(decisionService.getDecisionsByConference(conferenceId, chairId, isAdmin)));
  }

  @GetMapping("/pending-notifications")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Lấy danh sách decision chưa gửi thông báo
  public ResponseEntity<ApiResponse<List<DecisionResultDTO>>> getPendingNotifications() {
    return ResponseEntity.ok(ApiResponse.success(decisionService.getPendingNotifications()));
  }

  @PostMapping("/notify/{decisionId}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Gửi thông báo cho authors về decision
  public ResponseEntity<ApiResponse<Void>> sendNotification(@PathVariable Long decisionId) {
    com.uth.confms.decision.entity.Decision decision = decisionService.getDecisionEntityById(decisionId);
    notificationService.sendDecisionNotification(decision);
    return ResponseEntity.ok(ApiResponse.success("Notification sent", null));
  }

  @PostMapping("/notifications/bulk")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Gửi thông báo hàng loạt
  public ResponseEntity<ApiResponse<Void>> sendBulkNotifications(
      @Valid @RequestBody BulkNotificationRequestDTO dto) {
    notificationService.sendBulkNotifications(dto);
    return ResponseEntity.ok(ApiResponse.success("Bulk notifications sent", null));
  }

  @PutMapping("/{decisionId}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // Cập nhật decision (khi chưa khóa)
  public ResponseEntity<ApiResponse<DecisionResultDTO>> updateDecision(
      @PathVariable Long decisionId,
      @Valid @RequestBody com.uth.confms.decision.dto.UpdateDecisionRequestDTO dto,
      Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(decisionService.updateDecision(decisionId, dto, chairId, isAdmin)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
  }
}
