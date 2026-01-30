package com.uth.confms.submission.dto;

import java.time.LocalDateTime;

public class SubmissionFileDTO {
  private Long id;
  private Integer versionNumber; // Số phiên bản
  private String fileName; // Tên file
  private String filePath; // Đường dẫn
  private Long fileSize; // Kích thước
  private String contentType;
  private Boolean isCurrent; // Là version hiện tại?
  private LocalDateTime uploadedAt; // Thời gian upload
  private String uploadNote;

  public SubmissionFileDTO() {
  }

  public SubmissionFileDTO(
      Long id,
      Integer versionNumber,
      String fileName,
      String filePath,
      Long fileSize,
      String contentType,
      Boolean isCurrent,
      LocalDateTime uploadedAt,
      String uploadNote) {
    this.id = id;
    this.versionNumber = versionNumber;
    this.fileName = fileName;
    this.filePath = filePath;
    this.fileSize = fileSize;
    this.contentType = contentType;
    this.isCurrent = isCurrent;
    this.uploadedAt = uploadedAt;
    this.uploadNote = uploadNote;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getVersionNumber() {
    return versionNumber;
  }

  public void setVersionNumber(Integer versionNumber) {
    this.versionNumber = versionNumber;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
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
    private Integer versionNumber;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private Boolean isCurrent;
    private LocalDateTime uploadedAt;
    private String uploadNote;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder versionNumber(Integer versionNumber) {
      this.versionNumber = versionNumber;
      return this;
    }

    public Builder fileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public Builder filePath(String filePath) {
      this.filePath = filePath;
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

    public Builder isCurrent(Boolean isCurrent) {
      this.isCurrent = isCurrent;
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

    public SubmissionFileDTO build() {
      return new SubmissionFileDTO(
          id,
          versionNumber,
          fileName,
          filePath,
          fileSize,
          contentType,
          isCurrent,
          uploadedAt,
          uploadNote);
    }
  }
}
