package com.uth.confms.cameraready.controller;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.cameraready.dto.CameraReadyResponseDTO;
import com.uth.confms.cameraready.dto.CameraReadyUploadDTO;
import com.uth.confms.cameraready.entity.CameraReadySubmission;
import com.uth.confms.cameraready.service.CameraReadyService;
import com.uth.confms.common.dto.ApiResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller quản lý camera-ready submissions
 *
 * <p>Các endpoints:
 *
 * <ul>
 *   <li>POST /api/camera-ready/upload - Upload camera-ready PDF (AUTHOR)
 *   <li>POST /api/camera-ready/{id}/validate - Validate camera-ready (CHAIR/ADMIN)
 *   <li>POST /api/camera-ready/{id}/approve - Approve camera-ready (CHAIR/ADMIN)
 *   <li>GET /api/camera-ready/submission/{id} - Lấy camera-ready by submission (authenticated)
 *   <li>GET /api/camera-ready/pending-validations - Lấy pending validations (CHAIR/ADMIN)
 *   <li>GET /api/camera-ready/approved - Lấy approved camera-ready (CHAIR/ADMIN)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/camera-ready")
public class CameraReadyController {
  private final CameraReadyService cameraReadyService;
  private final UserRepository userRepository;

  public CameraReadyController(
      CameraReadyService cameraReadyService, UserRepository userRepository) {
    this.cameraReadyService = cameraReadyService;
    this.userRepository = userRepository;
  }

  @PostMapping("/upload")
  @PreAuthorize("hasRole('AUTHOR')")
  public ResponseEntity<ApiResponse<CameraReadyResponseDTO>> uploadCameraReady(
      @RequestParam("submissionId") Long submissionId,
      @RequestParam("pdfFile") MultipartFile pdfFile,
      Authentication authentication)
      throws IOException {
    Long authorId = getUserIdFromAuthentication(authentication);

    CameraReadyUploadDTO dto = new CameraReadyUploadDTO();
    dto.setSubmissionId(submissionId);
    dto.setPdfFile(pdfFile);

    return ResponseEntity.ok(
        ApiResponse.success(cameraReadyService.uploadCameraReady(dto, authorId)));
  }

  @PostMapping("/{id}/validate")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CameraReadyResponseDTO>> validateCameraReady(
      @PathVariable Long id,
      @RequestParam String validationNotes,
      @RequestParam String status,
      Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    CameraReadySubmission.ValidationStatus validationStatus =
        CameraReadySubmission.ValidationStatus.valueOf(status);

    return ResponseEntity.ok(
        ApiResponse.success(
            cameraReadyService.validateCameraReady(
                id, validationNotes, validationStatus, chairId)));
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CameraReadyResponseDTO>> approveCameraReady(
      @PathVariable Long id, Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(cameraReadyService.approveCameraReady(id, chairId)));
  }

  @GetMapping("/submission/{submissionId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<CameraReadyResponseDTO>> getCameraReadyBySubmission(
      @PathVariable Long submissionId, Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(cameraReadyService.getCameraReadyBySubmission(submissionId, userId)));
  }

  @GetMapping("/pending-validations")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<CameraReadyResponseDTO>>> getPendingValidations(
      Authentication authentication) {
    Long chairId = getUserIdFromAuthentication(authentication);
    return ResponseEntity.ok(
        ApiResponse.success(cameraReadyService.getPendingValidations(chairId)));
  }

  @GetMapping("/approved")
  @PreAuthorize("hasRole('CHAIR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<CameraReadyResponseDTO>>> getApprovedCameraReady() {
    return ResponseEntity.ok(ApiResponse.success(cameraReadyService.getApprovedCameraReady()));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return user.getId();
  }
}
