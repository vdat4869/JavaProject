package com.uth.confms.pc.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.pc.dto.COIDeclareDTO;
import com.uth.confms.pc.dto.PCInvitationResponseDTO;
import com.uth.confms.pc.dto.PCInviteDTO;
import com.uth.confms.pc.dto.PCMemberDTO;
import com.uth.confms.pc.entity.ConflictOfInterest;
import com.uth.confms.pc.service.COIService;
import com.uth.confms.pc.service.PCService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý Program Committee (PC) members và COI declarations
 *
 * <p>Các endpoints:
 *
 * <ul>
 *   <li>POST /api/pc/invite - Mời PC member (CHAIR/ADMIN)
 *   <li>POST /api/pc/invitation/accept - Chấp nhận invitation (authenticated)
 *   <li>POST /api/pc/invitation/decline - Từ chối invitation (authenticated)
 *   <li>GET /api/pc/conference/{id}/members - Lấy danh sách PC members (CHAIR/ADMIN)
 *   <li>GET /api/pc/conference/{id}/invitations - Lấy danh sách invitations (CHAIR/ADMIN)
 *   <li>POST /api/pc/coi/declare - Khai báo COI (PC/REVIEWER)
 *   <li>DELETE /api/pc/coi/{id} - Xóa COI (PC/REVIEWER)
 *   <li>GET /api/pc/coi/my - Lấy COIs của reviewer (PC/REVIEWER)
 *   <li>GET /api/pc/coi/submission/{id} - Lấy COIs của submission (CHAIR/ADMIN)
 *   <li>GET /api/pc/coi/check - Kiểm tra COI (PC/REVIEWER)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/pc")
public class PCController {
  private final PCService pcService;
  private final COIService coiService;
  private final UserService userService;

  public PCController(PCService pcService, COIService coiService, UserService userService) {
    this.pcService = pcService;
    this.coiService = coiService;
    this.userService = userService;
  }

  @PostMapping("/invite")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<PCInvitationResponseDTO>> invitePCMember(
      @Valid @RequestBody PCInviteDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.invitePCMember(dto, chairId)));
  }

  @PostMapping("/invitation/accept")
  @PreAuthorize("isAuthenticated()")
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
  public ResponseEntity<ApiResponse<List<PCMemberDTO>>> getPCMembers(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(pcService.getPCMembers(conferenceId, chairId)));
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
  public ResponseEntity<ApiResponse<ConflictOfInterest>> declareCOI(
      @Valid @RequestBody COIDeclareDTO dto, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(coiService.declareCOI(dto, reviewerId)));
  }

  @DeleteMapping("/coi/{coiId}")
  @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
  public ResponseEntity<ApiResponse<Void>> removeCOI(
      @PathVariable Long coiId, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    coiService.removeCOI(coiId, reviewerId);
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
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<ConflictOfInterest>>> getCOIsBySubmission(
      @PathVariable Long submissionId) {
    return ResponseEntity.ok(ApiResponse.success(coiService.getCOIsBySubmission(submissionId)));
  }

  @GetMapping("/coi/check")
  @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
  public ResponseEntity<ApiResponse<Boolean>> checkCOI(
      @RequestParam Long submissionId, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(coiService.hasCOI(reviewerId, submissionId)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
