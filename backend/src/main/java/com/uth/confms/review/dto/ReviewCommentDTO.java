package com.uth.confms.review.dto;

import java.time.LocalDateTime;

public class ReviewCommentDTO {
  private Long id;
  private Long submissionId;
  private Long reviewerId;
  private String reviewerName; // Chỉ hiển thị cho chair/admin đối với internal comment
  private String content; // Nội dung comment
  private Boolean isInternal; // Thảo luận nội bộ
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public ReviewCommentDTO() {
  }

  public ReviewCommentDTO(
      Long id,
      Long submissionId,
      Long reviewerId,
      String reviewerName,
      String content,
      Boolean isInternal,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.reviewerId = reviewerId;
    this.reviewerName = reviewerName;
    this.content = content;
    this.isInternal = isInternal;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public Long getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(Long reviewerId) {
    this.reviewerId = reviewerId;
  }

  public String getReviewerName() {
    return reviewerName;
  }

  public void setReviewerName(String reviewerName) {
    this.reviewerName = reviewerName;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Boolean getIsInternal() {
    return isInternal;
  }

  public void setIsInternal(Boolean isInternal) {
    this.isInternal = isInternal;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private String reviewerName;
    private String content;
    private Boolean isInternal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder reviewerId(Long reviewerId) {
      this.reviewerId = reviewerId;
      return this;
    }

    public Builder reviewerName(String reviewerName) {
      this.reviewerName = reviewerName;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder isInternal(Boolean isInternal) {
      this.isInternal = isInternal;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public ReviewCommentDTO build() {
      return new ReviewCommentDTO(
          id, submissionId, reviewerId, reviewerName, content, isInternal, createdAt, updatedAt);
    }
  }
}
