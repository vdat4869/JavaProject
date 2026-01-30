package com.uth.confms.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "review_comments")
@EntityListeners(AuditingEntityListener.class)
public class ReviewComment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long submissionId;

  @Column(nullable = false)
  private Long reviewerId; // Người comment

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content; // Nội dung comment

  @Column(nullable = false)
  private Boolean isInternal = true; // Thảo luận nội bộ (chỉ reviewer thấy)

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public ReviewComment() {
  }

  public ReviewComment(
      Long id,
      Long submissionId,
      Long reviewerId,
      String content,
      Boolean isInternal,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.reviewerId = reviewerId;
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
    private String content;
    private Boolean isInternal = true;
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

    public ReviewComment build() {
      return new ReviewComment(id, submissionId, reviewerId, content, isInternal, createdAt, updatedAt);
    }
  }
}
