package com.uth.confms.submission.controller;

import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.submission.dto.SubmissionCreateDTO;
import com.uth.confms.submission.dto.SubmissionFileDTO;
import com.uth.confms.submission.dto.SubmissionResponseDTO;
import com.uth.confms.submission.dto.SubmissionUpdateDTO;
import com.uth.confms.submission.service.SubmissionService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý submissions (bài nộp)
 *
 * <p>Các endpoints:
 *
 * <ul>
 *   <li>GET /api/submissions/my - Lấy danh sách submissions của author (AUTHOR)
 *   <li>GET /api/submissions/{id} - Lấy thông tin submission (AUTHOR)
 *   <li>POST /api/submissions - Tạo submission mới (AUTHOR)
 *   <li>PUT /api/submissions/{id} - Cập nhật submission (AUTHOR)
 *   <li>POST /api/submissions/{id}/submit - Submit submission (AUTHOR)
 *   <li>POST /api/submissions/{id}/withdraw - Withdraw submission (AUTHOR)
 *   <li>POST /api/submissions/{id}/upload-pdf - Upload PDF file (AUTHOR)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
  private final SubmissionService submissionService;
  private final UserService userService;

  public SubmissionController(SubmissionService submissionService, UserService userService) {
    this.submissionService = submissionService;
    this.userService = userService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> getMySubmissions(
      Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(submissionService.getMySubmissions(authorId)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<SubmissionResponseDTO>> getSubmission(
      @PathVariable Long id, Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(submissionService.getSubmission(id, authorId)));
  }

  @PostMapping
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<SubmissionResponseDTO>> createSubmission(
      @Valid @RequestBody SubmissionCreateDTO dto, Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(submissionService.createSubmission(dto, authorId)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<SubmissionResponseDTO>> updateSubmission(
      @PathVariable Long id,
      @Valid @RequestBody SubmissionUpdateDTO dto,
      Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(submissionService.updateSubmission(id, dto, authorId)));
  }

  @PostMapping("/{id}/submit")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<SubmissionResponseDTO>> submitSubmission(
      @PathVariable Long id, Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(submissionService.submitSubmission(id, authorId)));
  }

  @PostMapping("/{id}/withdraw")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<SubmissionResponseDTO>> withdrawSubmission(
      @PathVariable Long id, Authentication authentication) {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(submissionService.withdrawSubmission(id, authorId)));
  }

  @PostMapping("/{id}/upload-pdf")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<SubmissionFileDTO>> uploadPdf(
      @PathVariable Long id,
      @RequestParam("file") MultipartFile file,
      Authentication authentication)
      throws IOException {
    Long authorId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(ApiResponse.success(submissionService.uploadPdf(id, file, authorId)));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
