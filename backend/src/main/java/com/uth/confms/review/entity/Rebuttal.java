package com.uth.confms.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "rebuttals")
@EntityListeners(AuditingEntityListener.class)
public class Rebuttal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long submissionId; // ID bài báo

  @Column(nullable = false)
  private Long authorId; // ID tác giả

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content; // Nội dung phản biện

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private RebuttalStatus status = RebuttalStatus.DRAFT; // Trạng thái

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime submittedAt; // Thời gian nộp

  public Rebuttal() {
  }

  public Rebuttal(
      Long id,
      Long submissionId,
      Long authorId,
      String content,
      RebuttalStatus status,
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

  public RebuttalStatus getStatus() {
    return status;
  }

  public void setStatus(RebuttalStatus status) {
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

  public enum RebuttalStatus {
    DRAFT,
    SUBMITTED
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private Long authorId;
    private String content;
    private RebuttalStatus status = RebuttalStatus.DRAFT;
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

    public Builder status(RebuttalStatus status) {
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

    public Rebuttal build() {
      return new Rebuttal(id, submissionId, authorId, content, status, createdAt, submittedAt);
    }
  }
}
