package com.uth.confms.assignment.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.assignment.dto.AssignmentCreateDTO;
import com.uth.confms.assignment.dto.AssignmentResponseDTO;
import com.uth.confms.assignment.dto.AssignmentSuggestionDTO;
import com.uth.confms.assignment.service.AssignmentService;
import com.uth.confms.assignment.service.AssignmentSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý assignments (phân công reviewer)
 * 
 * <p>Các endpoints:
 * <ul>
 *   <li>POST /api/assignments - Tạo assignment (CHAIR/ADMIN)</li>
 *   <li>POST /api/assignments/{id}/accept - Chấp nhận assignment (PC/REVIEWER)</li>
 *   <li>POST /api/assignments/{id}/decline - Từ chối assignment (PC/REVIEWER)</li>
 *   <li>DELETE /api/assignments/{id} - Xóa assignment (CHAIR/ADMIN)</li>
 *   <li>GET /api/assignments/submission/{id} - Lấy assignments của submission (CHAIR/ADMIN)</li>
 *   <li>GET /api/assignments/my - Lấy assignments của reviewer (PC/REVIEWER)</li>
 *   <li>GET /api/assignments/{id} - Lấy assignment by ID (authenticated)</li>
 *   <li>GET /api/assignments/submission/{id}/suggestions - Lấy AI suggestions (CHAIR/ADMIN)</li>
 * </ul>
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final AssignmentSuggestionService suggestionService;
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> createAssignment(
            @Valid @RequestBody AssignmentCreateDTO dto,
            Authentication authentication) {
        Long chairId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.createAssignment(dto, chairId)));
    }
    
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> acceptAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        Long reviewerId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.acceptAssignment(id, reviewerId)));
    }
    
    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> declineAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        Long reviewerId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.declineAssignment(id, reviewerId)));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        Long chairId = getUserIdFromAuthentication(authentication);
        assignmentService.deleteAssignment(id, chairId);
        return ResponseEntity.ok(ApiResponse.success("Assignment deleted", null));
    }
    
    @GetMapping("/submission/{submissionId}")
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AssignmentResponseDTO>>> getAssignmentsBySubmission(
            @PathVariable Long submissionId,
            Authentication authentication) {
        Long chairId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.getAssignmentsBySubmission(submissionId, chairId)));
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('PC') or hasRole('REVIEWER')")
    public ResponseEntity<ApiResponse<List<AssignmentResponseDTO>>> getMyAssignments(
            Authentication authentication) {
        Long reviewerId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.getMyAssignments(reviewerId)));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AssignmentResponseDTO>> getAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                assignmentService.getAssignment(id, userId)));
    }
    
    @GetMapping("/submission/{submissionId}/suggestions")
    @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AssignmentSuggestionDTO>>> getSuggestions(
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(ApiResponse.success(
                suggestionService.getSuggestions(submissionId)));
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userService.getUserIdByEmail(email);
    }
}

