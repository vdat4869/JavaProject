package com.uth.confms.cameraready.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class CameraReadyUploadDTO {
  @NotNull(message = "Submission ID is required")
  private Long submissionId;

  @NotNull(message = "PDF file is required")
  private MultipartFile pdfFile;

  public CameraReadyUploadDTO() {}

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public MultipartFile getPdfFile() {
    return pdfFile;
  }

  public void setPdfFile(MultipartFile pdfFile) {
    this.pdfFile = pdfFile;
  }
}
