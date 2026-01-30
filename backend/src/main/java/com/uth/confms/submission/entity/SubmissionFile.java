package com.uth.confms.submission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "submission_files")
@EntityListeners(AuditingEntityListener.class)
public class SubmissionFile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "submission_id", nullable = false)
  private Submission submission;

  @Column(nullable = false)
  private Integer versionNumber; // Số phiên bản

  @Column(nullable = false)
  private String filePath; // Đường dẫn file

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long fileSize;

  @Column(nullable = false)
  private String contentType;

  private String checksum; // Checksum kiểm tra toàn vẹn

  @Column(nullable = false)
  private Boolean isCurrent = false; // Phiên bản hiện tại?

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  private String uploadNote;

  public SubmissionFile() {
    this.isCurrent = false;
  }

  public SubmissionFile(
      Long id,
      Submission submission,
      Integer versionNumber,
      String filePath,
      String fileName,
      Long fileSize,
      String contentType,
      String checksum,
      Boolean isCurrent,
      LocalDateTime uploadedAt,
      String uploadNote) {
    this.id = id;
    this.submission = submission;
    this.versionNumber = versionNumber;
    this.filePath = filePath;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.contentType = contentType;
    this.checksum = checksum;
    this.isCurrent = isCurrent != null ? isCurrent : false;
    this.uploadedAt = uploadedAt;
    this.uploadNote = uploadNote;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Submission getSubmission() {
    return submission;
  }

  public void setSubmission(Submission submission) {
    this.submission = submission;
  }

  public Integer getVersionNumber() {
    return versionNumber;
  }

  public void setVersionNumber(Integer versionNumber) {
    this.versionNumber = versionNumber;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public Boolean getIsCurrent() {
    return isCurrent;
  }

  public void setIsCurrent(Boolean isCurrent) {
    this.isCurrent = isCurrent;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public String getUploadNote() {
    return uploadNote;
  }

  public void setUploadNote(String uploadNote) {
    this.uploadNote = uploadNote;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Submission submission;
    private Integer versionNumber;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String checksum;
    private Boolean isCurrent = false;
    private LocalDateTime uploadedAt;
    private String uploadNote;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submission(Submission submission) {
      this.submission = submission;
      return this;
    }

    public Builder versionNumber(Integer versionNumber) {
      this.versionNumber = versionNumber;
      return this;
    }

    public Builder filePath(String filePath) {
      this.filePath = filePath;
      return this;
    }

    public Builder fileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public Builder fileSize(Long fileSize) {
      this.fileSize = fileSize;
      return this;
    }

    public Builder contentType(String contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder checksum(String checksum) {
      this.checksum = checksum;
      return this;
    }

    public Builder isCurrent(Boolean isCurrent) {
      this.isCurrent = isCurrent != null ? isCurrent : false;
      return this;
    }

    public Builder uploadedAt(LocalDateTime uploadedAt) {
      this.uploadedAt = uploadedAt;
      return this;
    }

    public Builder uploadNote(String uploadNote) {
      this.uploadNote = uploadNote;
      return this;
    }

    public SubmissionFile build() {
      return new SubmissionFile(
          id,
          submission,
          versionNumber,
          filePath,
          fileName,
          fileSize,
          contentType,
          checksum,
          isCurrent,
          uploadedAt,
          uploadNote);
    }
  }
}
