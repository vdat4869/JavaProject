package com.uth.confms.cameraready.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "camera_ready_submissions")
@EntityListeners(AuditingEntityListener.class)
public class CameraReadySubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long submissionId;

  @Column(nullable = false)
  private String pdfFilePath;

  @Column(nullable = false)
  private Long fileSize;

  private String checksum;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ValidationStatus validationStatus = ValidationStatus.PENDING;

  @Column(columnDefinition = "TEXT")
  private String validationNotes;

  @Column(nullable = false)
  private Boolean approved = false;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  public CameraReadySubmission() {
    this.validationStatus = ValidationStatus.PENDING;
    this.approved = false;
  }

  public CameraReadySubmission(
      Long id,
      Long submissionId,
      String pdfFilePath,
      Long fileSize,
      String checksum,
      ValidationStatus validationStatus,
      String validationNotes,
      Boolean approved,
      LocalDateTime uploadedAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.pdfFilePath = pdfFilePath;
    this.fileSize = fileSize;
    this.checksum = checksum;
    this.validationStatus = validationStatus != null ? validationStatus : ValidationStatus.PENDING;
    this.validationNotes = validationNotes;
    this.approved = approved != null ? approved : false;
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

  public ValidationStatus getValidationStatus() {
    return validationStatus;
  }

  public void setValidationStatus(ValidationStatus validationStatus) {
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

  public enum ValidationStatus {
    PENDING,
    VALID,
    INVALID,
    REQUIRES_REVISION
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private String pdfFilePath;
    private Long fileSize;
    private String checksum;
    private ValidationStatus validationStatus = ValidationStatus.PENDING;
    private String validationNotes;
    private Boolean approved = false;
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

    public Builder validationStatus(ValidationStatus validationStatus) {
      this.validationStatus =
          validationStatus != null ? validationStatus : ValidationStatus.PENDING;
      return this;
    }

    public Builder validationNotes(String validationNotes) {
      this.validationNotes = validationNotes;
      return this;
    }

    public Builder approved(Boolean approved) {
      this.approved = approved != null ? approved : false;
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

    public CameraReadySubmission build() {
      return new CameraReadySubmission(
          id,
          submissionId,
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
