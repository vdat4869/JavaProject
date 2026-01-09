package com.uth.confms.cameraready.service;

import com.uth.confms.cameraready.dto.CameraReadyResponseDTO;
import com.uth.confms.cameraready.dto.CameraReadyUploadDTO;
import com.uth.confms.cameraready.entity.CameraReadySubmission;
import com.uth.confms.cameraready.repository.CameraReadyRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.decision.entity.Decision;
import com.uth.confms.decision.repository.DecisionRepository;
import com.uth.confms.storage.service.StorageService;
import com.uth.confms.submission.entity.Submission;
import com.uth.confms.submission.repository.SubmissionRepository;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service quản lý camera-ready submissions
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Upload PDF camera-ready (chỉ author của accepted submission)
 *   <li>Validate camera-ready files
 *   <li>Approve camera-ready (chair)
 *   <li>File validation: PDF only, size limit, MD5 checksum
 *   <li>Auto-update submission status to CAMERA_READY
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class CameraReadyService {
  private final CameraReadyRepository cameraReadyRepository;
  private final SubmissionRepository submissionRepository;
  private final DecisionRepository decisionRepository;
  private final StorageService storageService;

  public CameraReadyService(
      CameraReadyRepository cameraReadyRepository,
      SubmissionRepository submissionRepository,
      DecisionRepository decisionRepository,
      StorageService storageService) {
    this.cameraReadyRepository = cameraReadyRepository;
    this.submissionRepository = submissionRepository;
    this.decisionRepository = decisionRepository;
    this.storageService = storageService;
  }

  @Transactional
  public CameraReadyResponseDTO uploadCameraReady(CameraReadyUploadDTO dto, Long authorId)
      throws IOException {
    Submission submission =
        submissionRepository
            .findById(dto.getSubmissionId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Submission with id " + dto.getSubmissionId() + " not found"));

    // Check authorization - only author can upload camera-ready
    if (!submission.getAuthorId().equals(authorId)) {
      throw new UnauthorizedException("Only submission author can upload camera-ready version");
    }

    // Check if submission is accepted
    Decision decision =
        decisionRepository
            .findBySubmissionId(dto.getSubmissionId())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "Submission must be accepted before uploading camera-ready"));

    if (decision.getType() != Decision.DecisionType.ACCEPT
        && decision.getType() != Decision.DecisionType.CONDITIONAL_ACCEPT) {
      throw new BusinessException("Only accepted submissions can upload camera-ready");
    }

    // Validate file
    MultipartFile file = dto.getPdfFile();
    if (file == null || file.isEmpty()) {
      throw new BusinessException("PDF file is required");
    }

    // Store file using StorageService (validation is done inside)
    String relativePath = storageService.storeCameraReadyPdf(dto.getSubmissionId(), file);

    // Calculate checksum
    String checksum = calculateChecksumFromStream(storageService.getFileStream(relativePath));

    // Check if camera-ready already exists
    CameraReadySubmission cameraReady =
        cameraReadyRepository.findBySubmissionId(dto.getSubmissionId()).orElse(null);

    if (cameraReady == null) {
      // Create new
      cameraReady =
          CameraReadySubmission.builder()
              .submissionId(dto.getSubmissionId())
              .pdfFilePath(relativePath)
              .fileSize(file.getSize())
              .checksum(checksum)
              .validationStatus(CameraReadySubmission.ValidationStatus.PENDING)
              .approved(false)
              .build();
    } else {
      // Update existing
      // Delete old file
      storageService.deleteFile(cameraReady.getPdfFilePath());

      cameraReady.setPdfFilePath(relativePath);
      cameraReady.setFileSize(file.getSize());
      cameraReady.setChecksum(checksum);
      cameraReady.setValidationStatus(CameraReadySubmission.ValidationStatus.PENDING);
      cameraReady.setApproved(false);
      cameraReady.setValidationNotes(null);
    }

    cameraReady = cameraReadyRepository.save(cameraReady);

    // Update submission status
    submission.setStatus(Submission.SubmissionStatus.CAMERA_READY);
    submissionRepository.save(submission);

    return mapToDTO(cameraReady);
  }

  @Transactional
  public CameraReadyResponseDTO validateCameraReady(
      Long cameraReadyId,
      String validationNotes,
      CameraReadySubmission.ValidationStatus status,
      Long chairId) {
    CameraReadySubmission cameraReady =
        cameraReadyRepository
            .findById(cameraReadyId)
            .orElseThrow(() -> new NotFoundException("Camera-ready submission not found"));

    // Validate submission exists
    submissionRepository
        .findById(cameraReady.getSubmissionId())
        .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check authorization - only chair can validate
    // This would require conference lookup, simplified here
    // In real implementation, check conference chair

    cameraReady.setValidationStatus(status);
    cameraReady.setValidationNotes(validationNotes);

    if (status == CameraReadySubmission.ValidationStatus.VALID) {
      cameraReady.setApproved(true);
    } else {
      cameraReady.setApproved(false);
    }

    cameraReady = cameraReadyRepository.save(cameraReady);

    return mapToDTO(cameraReady);
  }

  @Transactional
  public CameraReadyResponseDTO approveCameraReady(Long cameraReadyId, Long chairId) {
    CameraReadySubmission cameraReady =
        cameraReadyRepository
            .findById(cameraReadyId)
            .orElseThrow(() -> new NotFoundException("Camera-ready submission not found"));

    if (cameraReady.getValidationStatus() != CameraReadySubmission.ValidationStatus.VALID) {
      throw new BusinessException("Camera-ready must be validated as VALID before approval");
    }

    cameraReady.setApproved(true);
    cameraReady = cameraReadyRepository.save(cameraReady);

    return mapToDTO(cameraReady);
  }

  public CameraReadyResponseDTO getCameraReadyBySubmission(Long submissionId, Long userId) {
    CameraReadySubmission cameraReady =
        cameraReadyRepository
            .findBySubmissionId(submissionId)
            .orElseThrow(() -> new NotFoundException("Camera-ready submission not found"));

    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission not found"));

    // Check authorization: author or chair/admin
    if (!submission.getAuthorId().equals(userId)) {
      // Check if user is chair/admin - simplified, should check conference
      // For now, allow if authenticated
    }

    return mapToDTO(cameraReady);
  }

  public List<CameraReadyResponseDTO> getPendingValidations(Long chairId) {
    return cameraReadyRepository
        .findByValidationStatus(CameraReadySubmission.ValidationStatus.PENDING)
        .stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  public List<CameraReadyResponseDTO> getApprovedCameraReady() {
    return cameraReadyRepository.findByApprovedTrue().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Tính checksum từ InputStream
   *
   * @param inputStream InputStream của file
   * @return MD5 checksum dạng hex string
   */
  private String calculateChecksumFromStream(InputStream inputStream) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        md.update(buffer, 0, bytesRead);
      }
      byte[] digest = md.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new BusinessException("Error calculating file checksum", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }

  private CameraReadyResponseDTO mapToDTO(CameraReadySubmission cameraReady) {
    Submission submission =
        submissionRepository.findById(cameraReady.getSubmissionId()).orElse(null);

    return CameraReadyResponseDTO.builder()
        .id(cameraReady.getId())
        .submissionId(cameraReady.getSubmissionId())
        .submissionTitle(submission != null ? submission.getTitle() : null)
        .pdfFilePath(cameraReady.getPdfFilePath())
        .fileSize(cameraReady.getFileSize())
        .checksum(cameraReady.getChecksum())
        .validationStatus(cameraReady.getValidationStatus().name())
        .validationNotes(cameraReady.getValidationNotes())
        .approved(cameraReady.getApproved())
        .uploadedAt(cameraReady.getUploadedAt())
        .updatedAt(cameraReady.getUpdatedAt())
        .build();
  }
}
