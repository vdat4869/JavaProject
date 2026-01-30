package com.uth.confms.decision.dto;

import java.time.LocalDateTime;

public class DecisionResultDTO {
  private Long id;
  private Long submissionId;
  private String submissionTitle;
  private Long decidedBy; // ID người quyết định
  private String decidedByName; // Tên người quyết định
  private String type; // Loại quyết định
  private String comments; // Nhận xét
  private Boolean notified; // Đã gửi thông báo
  private Boolean locked; // Đã khóa
  private LocalDateTime decidedAt; // Thời gian quyết định
  private ReviewSummaryDTO reviewSummary; // Tóm tắt review

  public DecisionResultDTO() {
  }

  public DecisionResultDTO(
      Long id,
      Long submissionId,
      String submissionTitle,
      Long decidedBy,
      String decidedByName,
      String type,
      String comments,
      Boolean notified,
      Boolean locked,
      LocalDateTime decidedAt,
      ReviewSummaryDTO reviewSummary) {
    this.id = id;
    this.submissionId = submissionId;
    this.submissionTitle = submissionTitle;
    this.decidedBy = decidedBy;
    this.decidedByName = decidedByName;
    this.type = type;
    this.comments = comments;
    this.notified = notified;
    this.locked = locked;
    this.decidedAt = decidedAt;
    this.reviewSummary = reviewSummary;
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

  public String getSubmissionTitle() {
    return submissionTitle;
  }

  public void setSubmissionTitle(String submissionTitle) {
    this.submissionTitle = submissionTitle;
  }

  public Long getDecidedBy() {
    return decidedBy;
  }

  public void setDecidedBy(Long decidedBy) {
    this.decidedBy = decidedBy;
  }

  public String getDecidedByName() {
    return decidedByName;
  }

  public void setDecidedByName(String decidedByName) {
    this.decidedByName = decidedByName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Boolean getNotified() {
    return notified;
  }

  public void setNotified(Boolean notified) {
    this.notified = notified;
  }

  public Boolean getLocked() {
    return locked;
  }

  public void setLocked(Boolean locked) {
    this.locked = locked;
  }

  public LocalDateTime getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(LocalDateTime decidedAt) {
    this.decidedAt = decidedAt;
  }

  public ReviewSummaryDTO getReviewSummary() {
    return reviewSummary;
  }

  public void setReviewSummary(ReviewSummaryDTO reviewSummary) {
    this.reviewSummary = reviewSummary;
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private String submissionTitle;
    private Long decidedBy;
    private String decidedByName;
    private String type;
    private String comments;
    private Boolean notified;
    private Boolean locked;
    private LocalDateTime decidedAt;
    private ReviewSummaryDTO reviewSummary;

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

    public Builder decidedBy(Long decidedBy) {
      this.decidedBy = decidedBy;
      return this;
    }

    public Builder decidedByName(String decidedByName) {
      this.decidedByName = decidedByName;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder comments(String comments) {
      this.comments = comments;
      return this;
    }

    public Builder notified(Boolean notified) {
      this.notified = notified;
      return this;
    }

    public Builder locked(Boolean locked) {
      this.locked = locked;
      return this;
    }

    public Builder decidedAt(LocalDateTime decidedAt) {
      this.decidedAt = decidedAt;
      return this;
    }

    public Builder reviewSummary(ReviewSummaryDTO reviewSummary) {
      this.reviewSummary = reviewSummary;
      return this;
    }

    public DecisionResultDTO build() {
      return new DecisionResultDTO(
          id,
          submissionId,
          submissionTitle,
          decidedBy,
          decidedByName,
          type,
          comments,
          notified,
          locked,
          decidedAt,
          reviewSummary);
    }
  }
}
