package com.uth.confms.review.dto;

import java.time.LocalDateTime;

public class RebuttalDTO {
  private Long id;
  private Long submissionId;
  private Long authorId;
  private String content; // Nội dung phản biện
  private String status; // Trạng thái
  private LocalDateTime createdAt;
  private LocalDateTime submittedAt;

  public RebuttalDTO() {
  }

  public RebuttalDTO(
      Long id,
      Long submissionId,
      Long authorId,
      String content,
      String status,
      LocalDateTime createdAt,
      LocalDateTime submittedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.authorId = authorId;
    this.content = content;
    this.status = status;
    this.createdAt = createdAt;
    this.submittedAt = submittedAt;
  }

  public static Builder builder() {
    return new Builder();
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

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private Long authorId;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder authorId(Long authorId) {
      this.authorId = authorId;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder submittedAt(LocalDateTime submittedAt) {
      this.submittedAt = submittedAt;
      return this;
    }

    public RebuttalDTO build() {
      return new RebuttalDTO(id, submissionId, authorId, content, status, createdAt, submittedAt);
    }
  }
}
