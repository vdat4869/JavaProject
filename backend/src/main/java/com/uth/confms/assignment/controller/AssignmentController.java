package com.uth.confms.assignment.controller;

import com.uth.confms.assignment.dto.AssignmentCreateDTO;
import com.uth.confms.assignment.dto.AssignmentResponseDTO;
import com.uth.confms.assignment.dto.AssignmentSuggestionDTO;
import com.uth.confms.assignment.dto.AssignmentStatisticsDTO;
import com.uth.confms.assignment.dto.AssignmentQualityMetricsDTO;
import com.uth.confms.assignment.dto.AutoAssignRequestDTO;
import com.uth.confms.assignment.dto.AutoAssignResponseDTO;
import com.uth.confms.assignment.dto.BulkAssignRequestDTO;
import com.uth.confms.assignment.dto.BulkAssignResponseDTO;
import com.uth.confms.assignment.dto.ReassignRequestDTO;
import com.uth.confms.assignment.service.AssignmentService;
import com.uth.confms.assignment.service.AssignmentSuggestionService;
import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller quản lý assignments (phân công reviewer)
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>POST /api/assignments - Tạo assignment (CHAIR/ADMIN)
 * <li>POST /api/assignments/{id}/accept - Chấp nhận assignment (PC/REVIEWER)
 * <li>POST /api/assignments/{id}/decline - Từ chối assignment (PC/REVIEWER)
 * <li>DELETE /api/assignments/{id} - Xóa assignment (CHAIR/ADMIN)
 * <li>GET /api/assignments/submission/{id} - Lấy assignments của submission
 * (CHAIR/ADMIN)
 * <li>GET /api/assignments/my - Lấy assignments của reviewer (PC/REVIEWER)
 * <li>GET /api/assignments/{id} - Lấy assignment by ID (authenticated)
 * <li>GET /api/assignments/submission/{id}/suggestions - Lấy AI suggestions
 * (CHAIR/ADMIN)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
  private final AssignmentService assignmentService;
  private final AssignmentSuggestionService suggestionService;
  private final UserService userService;

  public AssignmentController(
      AssignmentService assignmentService,
      AssignmentSuggestionService suggestionService,
      UserService userService) {
    this.assignmentService = assignmentService;
    this.suggestionService = suggestionService;
    this.userService = userService;
  }

  @PostMapping
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AssignmentResponseDTO>> createAssignment(
      @Valid @RequestBody AssignmentCreateDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(ApiResponse.success(assignmentService.createAssignment(dto, chairId, isAdmin)));
  }

  @PostMapping("/{id}/accept")
  @PreAuthorize("hasRole('PC')")
  public ResponseEntity<ApiResponse<AssignmentResponseDTO>> acceptAssignment(
      @PathVariable Long id, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(assignmentService.acceptAssignment(id, reviewerId)));
  }

  @PostMapping("/{id}/decline")
  @PreAuthorize("hasRole('PC')")
  public ResponseEntity<ApiResponse<AssignmentResponseDTO>> declineAssignment(
      @PathVariable Long id, Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(assignmentService.declineAssignment(id, reviewerId)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteAssignment(
      @PathVariable Long id, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    assignmentService.deleteAssignment(id, chairId, isAdmin);
    return ResponseEntity.ok(ApiResponse.success("Assignment deleted", null));
  }

  @GetMapping("/submission/{submissionId}")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<AssignmentResponseDTO>>> getAssignmentsBySubmission(
      @PathVariable Long submissionId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(assignmentService.getAssignmentsBySubmission(submissionId, chairId, isAdmin)));
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('PC')")
  public ResponseEntity<ApiResponse<List<AssignmentResponseDTO>>> getMyAssignments(
      Authentication authentication) {
    Long reviewerId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(assignmentService.getMyAssignments(reviewerId)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<AssignmentResponseDTO>> getAssignment(
      @PathVariable Long id, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignment(id, userId)));
  }

  @GetMapping("/submission/{submissionId}/suggestions")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<AssignmentSuggestionDTO>>> getSuggestions(
      @PathVariable Long submissionId) {
    return ResponseEntity.ok(ApiResponse.success(suggestionService.getSuggestions(submissionId)));
  }

  @PostMapping("/auto-assign")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AutoAssignResponseDTO>> autoAssign(
      @Valid @RequestBody AutoAssignRequestDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(ApiResponse.success(assignmentService.autoAssign(dto, chairId, isAdmin)));
  }

  @PostMapping("/bulk")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BulkAssignResponseDTO>> bulkAssign(
      @Valid @RequestBody BulkAssignRequestDTO dto, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(ApiResponse.success(assignmentService.bulkAssign(dto, chairId, isAdmin)));
  }

  @PutMapping("/{id}/reassign")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AssignmentResponseDTO>> reassignAssignment(
      @PathVariable Long id,
      @Valid @RequestBody ReassignRequestDTO dto,
      Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(assignmentService.reassignAssignment(id, dto, chairId, isAdmin)));
  }

  @GetMapping("/conference/{conferenceId}/statistics")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AssignmentStatisticsDTO>> getAssignmentStatistics(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(assignmentService.getAssignmentStatistics(conferenceId, chairId, isAdmin)));
  }

  @GetMapping("/conference/{conferenceId}/quality-metrics")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<AssignmentQualityMetricsDTO>> getAssignmentQualityMetrics(
      @PathVariable Long conferenceId, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    boolean isAdmin = isAdmin(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(assignmentService.getAssignmentQualityMetrics(conferenceId, chairId, isAdmin)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }

  private boolean isAdmin(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
  }
}
