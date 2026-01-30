package com.uth.confms.pc.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.pc.dto.COIDeclareDTO;
import com.uth.confms.pc.dto.PCInvitationResponseDTO;
import com.uth.confms.pc.dto.PCInviteDTO;
import com.uth.confms.pc.dto.COIHistoryDTO;
import com.uth.confms.pc.dto.COIStatisticsDTO;
import com.uth.confms.pc.dto.PCMemberDTO;
import com.uth.confms.pc.dto.WorkloadAlertDTO;
import com.uth.confms.pc.dto.WorkloadDTO;
import com.uth.confms.pc.dto.WorkloadStatsDTO;
import com.uth.confms.pc.entity.ConflictOfInterest;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.pc.service.PCService;
import com.uth.confms.pc.service.WorkloadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller quản lý Program Committee (PC) members và COI declarations
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>POST /api/pc/invite - Mời PC member (CHAIR/ADMIN)
 * <li>POST /api/pc/invitation/accept - Chấp nhận invitation (authenticated)
 * <li>POST /api/pc/invitation/decline - Từ chối invitation (authenticated)
 * <li>GET /api/pc/conference/{id}/members - Lấy danh sách PC members
 * (CHAIR/ADMIN)
 * <li>GET /api/pc/conference/{id}/invitations - Lấy danh sách invitations
 * (CHAIR/ADMIN)
 * <li>POST /api/pc/coi/declare - Khai báo COI (PC/REVIEWER)
 * <li>DELETE /api/pc/coi/{id} - Xóa COI (PC/REVIEWER)
 * <li>GET /api/pc/coi/my - Lấy COIs của reviewer (PC/REVIEWER)
 * <li>GET /api/pc/coi/submission/{id} - Lấy COIs của submission (CHAIR/ADMIN)
 * <li>GET /api/pc/coi/check - Kiểm tra COI (PC/REVIEWER)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/pc")
public class PCController {
  private static final Logger logger = LoggerFactory.getLogger(PCController.class);

  private final PCService pcService;
  private final COIService coiService;
  private final UserService userService;
  private final WorkloadService workloadService;

  public PCController(
      PCService pcService,
      COIService coiService,
      UserService userService,
      WorkloadService workloadService) {
    this.pcService = pcService;
    this.coiService = coiService;
    this.userService = userService;
    this.workloadService = workloadService;
  }

  @PostMapping("/invite")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API mời PC member (Chỉ Chair/Admin)
  public ResponseEntity<ApiResponse<PCInvitationResponseDTO>> invitePCMember(
      @Valid @RequestBody PCInviteDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.invitePCMember(dto, chairId)));
  }

  @PostMapping("/invitation/accept")
  @PreAuthorize("isAuthenticated()")
  // API chấp nhận lời mời tham gia PC
  public ResponseEntity<ApiResponse<PCMemberDTO>> acceptInvitation(
      @RequestParam String token, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.acceptInvitation(token, userId)));
  }

  @PostMapping("/invitation/decline")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> declineInvitation(
      @RequestParam String token, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    pcService.declineInvitation(token, userId);
    return ResponseEntity.ok(ApiResponse.success("Invitation declined", null));
  }

  @GetMapping("/conference/{conferenceId}/members")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API lấy danh sách PC members của conference
  public ResponseEntity<ApiResponse<List<PCMemberDTO>>> getPCMembers(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.getPCMembers(conferenceId, chairId)));
  }

  @GetMapping("/conference/{conferenceId}/membership")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PCMemberDTO>> getMyMembership(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.getMembership(conferenceId, userId)));
  }

  @GetMapping("/conference/{conferenceId}/invitations")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<PCInvitationResponseDTO>>> getInvitations(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.getInvitations(conferenceId, chairId)));
  }

  @PostMapping("/coi/declare")
  @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
  // API tự khai báo mâu thuẫn lợi ích (COI)
  public ResponseEntity<ApiResponse<ConflictOfInterest>> declareCOI(
      @Valid @RequestBody COIDeclareDTO dto,
      Authentication authentication,
      HttpServletRequest request) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(coiService.declareCOI(dto, reviewerId, request)));
  }

  @DeleteMapping("/coi/{coiId}")
  @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
  public ResponseEntity<ApiResponse<Void>> removeCOI(
      @PathVariable Long coiId,
      Authentication authentication,
      HttpServletRequest request) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    coiService.removeCOI(coiId, reviewerId, request);
    return ResponseEntity.ok(ApiResponse.success("COI removed", null));
  }

  @GetMapping("/coi/my")
  @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
  public ResponseEntity<ApiResponse<List<ConflictOfInterest>>> getMyCOIs(
      Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(coiService.getCOIsByReviewer(reviewerId)));
  }

  @GetMapping("/coi/submission/{submissionId}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN') or hasRole('PC') or hasRole('REVIEWER')")
  public ResponseEntity<ApiResponse<List<ConflictOfInterest>>> getCOIsBySubmission(
      @PathVariable Long submissionId, Authentication authentication) {
    logger.debug("DEBUG: getCOIsBySubmission called for submissionId: {}", submissionId);
    logger.debug("DEBUG: User authorities: {}", authentication.getAuthorities());
    List<ConflictOfInterest> cois = coiService.getCOIsBySubmission(submissionId);
    Long userId = getUserIdFromAuthentication(authentication);
    logger.debug("DEBUG: userId: {}", userId);

    // If user is not CHAIR or ADMIN, filter to show only their own COIs
    boolean isChairOrAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("CHAIR") || a.getAuthority().equals("ADMIN"));
    logger.debug("DEBUG: isChairOrAdmin: {}", isChairOrAdmin);

    if (!isChairOrAdmin) {
      cois = cois.stream()
          .filter(c -> c.getReviewerId().equals(userId))
          .toList();
    }

    return ResponseEntity.ok(ApiResponse.success(cois));
  }

  @GetMapping("/coi/check")
  @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
  public ResponseEntity<ApiResponse<Boolean>> checkCOI(
      @RequestParam Long submissionId, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(coiService.hasCOI(reviewerId, submissionId)));
  }

  @GetMapping("/reviewer/{reviewerId}/workload")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN') or hasRole('PC') or hasRole('REVIEWER')")
  // API xem chi tiết workload của reviewer
  public ResponseEntity<ApiResponse<WorkloadDTO>> getReviewerWorkload(
      @PathVariable Long reviewerId,
      @RequestParam Long conferenceId,
      Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    // Reviewer can only view their own workload, chair/admin can view any
    // Simplified check - in production, use proper authorization service
    if (!userId.equals(reviewerId)) {
      // Allow if user is chair of the conference (check would be done in service if
      // needed)
      // For now, allow all authenticated users with PC/REVIEWER role
    }
    return ResponseEntity.ok(
        ApiResponse.success(workloadService.getReviewerWorkload(reviewerId, conferenceId)));
  }

  @GetMapping("/conference/{conferenceId}/workload-stats")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<WorkloadStatsDTO>> getConferenceWorkloadStats(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(workloadService.getConferenceWorkloadStats(conferenceId, chairId)));
  }

  @GetMapping("/conference/{conferenceId}/workload-alerts")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  // API xem danh sách cảnh báo quá tải workload
  public ResponseEntity<ApiResponse<List<WorkloadAlertDTO>>> getWorkloadAlerts(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(workloadService.getWorkloadAlerts(conferenceId, chairId)));
  }

  @GetMapping("/conference/{conferenceId}/coi/history")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<COIHistoryDTO>>> getCOIHistory(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(coiService.getCOIHistory(conferenceId, chairId)));
  }

  @GetMapping("/conference/{conferenceId}/coi/statistics")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<COIStatisticsDTO>> getCOIStatistics(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(coiService.getCOIStatistics(conferenceId, chairId)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
