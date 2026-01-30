package com.uth.confms.cameraready.dto;

import java.time.LocalDateTime;

public class CameraReadyResponseDTO {
  private Long id;
  private Long submissionId;
  private String submissionTitle; // Tiêu đề bài báo
  private String pdfFilePath; // Đường dẫn file PDF
  private Long fileSize; // Kích thước file (bytes)
  private String checksum; // Checksum SHA-256
  private String validationStatus; // Trạng thái validate
  private String validationNotes; // Ghi chú validate
  private Boolean approved; // Đã được duyệt chưa
  private LocalDateTime uploadedAt; // Thời gian tải lên
  private LocalDateTime updatedAt; // Thời gian cập nhật

  public CameraReadyResponseDTO() {
  }

  public CameraReadyResponseDTO(
      Long id,
      Long submissionId,
      String submissionTitle,
      String pdfFilePath,
      Long fileSize,
      String checksum,
      String validationStatus,
      String validationNotes,
      Boolean approved,
      LocalDateTime uploadedAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.submissionTitle = submissionTitle;
    this.pdfFilePath = pdfFilePath;
    this.fileSize = fileSize;
    this.checksum = checksum;
    this.validationStatus = validationStatus;
    this.validationNotes = validationNotes;
    this.approved = approved;
    this.uploadedAt = uploadedAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public String getSubmissionTitle() {
    return submissionTitle;
  }

  public void setSubmissionTitle(String submissionTitle) {
    this.submissionTitle = submissionTitle;
  }

  public String getPdfFilePath() {
    return pdfFilePath;
  }

  public void setPdfFilePath(String pdfFilePath) {
    this.pdfFilePath = pdfFilePath;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public String getValidationStatus() {
    return validationStatus;
  }

  public void setValidationStatus(String validationStatus) {
    this.validationStatus = validationStatus;
  }

  public String getValidationNotes() {
    return validationNotes;
  }

  public void setValidationNotes(String validationNotes) {
    this.validationNotes = validationNotes;
  }

  public Boolean getApproved() {
    return approved;
  }

  public void setApproved(Boolean approved) {
    this.approved = approved;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private String submissionTitle;
    private String pdfFilePath;
    private Long fileSize;
    private String checksum;
    private String validationStatus;
    private String validationNotes;
    private Boolean approved;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder submissionTitle(String submissionTitle) {
      this.submissionTitle = submissionTitle;
      return this;
    }

    public Builder pdfFilePath(String pdfFilePath) {
      this.pdfFilePath = pdfFilePath;
      return this;
    }

    public Builder fileSize(Long fileSize) {
      this.fileSize = fileSize;
      return this;
    }

    public Builder checksum(String checksum) {
      this.checksum = checksum;
      return this;
    }

    public Builder validationStatus(String validationStatus) {
      this.validationStatus = validationStatus;
      return this;
    }

    public Builder validationNotes(String validationNotes) {
      this.validationNotes = validationNotes;
      return this;
    }

    public Builder approved(Boolean approved) {
      this.approved = approved;
      return this;
    }

    public Builder uploadedAt(LocalDateTime uploadedAt) {
      this.uploadedAt = uploadedAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public CameraReadyResponseDTO build() {
      return new CameraReadyResponseDTO(
          id,
          submissionId,
          submissionTitle,
          pdfFilePath,
          fileSize,
          checksum,
          validationStatus,
          validationNotes,
          approved,
          uploadedAt,
          updatedAt);
    }
  }
}
