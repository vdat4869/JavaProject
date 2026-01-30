package com.uth.confms.review.dto;

import java.time.LocalDateTime;

public class ReviewResponseDTO {
  private Long id;
  private Long assignmentId;
  private Long submissionId;
  private Long reviewerId;
  private String reviewerName; // Chỉ hiện cho chair/admin hoặc trong single-blind
  private String summary; // Tóm tắt
  private String strengths; // Điểm mạnh
  private String weaknesses; // Điểm yếu
  private String comments; // Nhận xét
  private String score; // Điểm đánh giá
  private String status; // Trạng thái
  private Boolean isConfidential; // Bí mật
  private Integer overallRating; // Đánh giá chung (1-5)
  private Integer confidence; // Độ tự tin (1-5)
  private Integer numericScore; // Điểm số (1-7)
  private LocalDateTime createdAt;
  private LocalDateTime submittedAt;

  public ReviewResponseDTO() {
  }

  public ReviewResponseDTO(
      Long id,
      Long assignmentId,
      Long submissionId,
      Long reviewerId,
      String reviewerName,
      String summary,
      String strengths,
      String weaknesses,
      String comments,
      String score,
      String status,
      Boolean isConfidential,
      Integer overallRating,
      Integer confidence,
      Integer numericScore,
      LocalDateTime createdAt,
      LocalDateTime submittedAt) {
    this.id = id;
    this.assignmentId = assignmentId;
    this.submissionId = submissionId;
    this.reviewerId = reviewerId;
    this.reviewerName = reviewerName;
    this.summary = summary;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.comments = comments;
    this.score = score;
    this.status = status;
    this.isConfidential = isConfidential;
    this.overallRating = overallRating;
    this.confidence = confidence;
    this.numericScore = numericScore;
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

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
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

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getStrengths() {
    return strengths;
  }

  public void setStrengths(String strengths) {
    this.strengths = strengths;
  }

  public String getWeaknesses() {
    return weaknesses;
  }

  public void setWeaknesses(String weaknesses) {
    this.weaknesses = weaknesses;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getIsConfidential() {
    return isConfidential;
  }

  public void setIsConfidential(Boolean isConfidential) {
    this.isConfidential = isConfidential;
  }

  public Integer getOverallRating() {
    return overallRating;
  }

  public void setOverallRating(Integer overallRating) {
    this.overallRating = overallRating;
  }

  public Integer getConfidence() {
    return confidence;
  }

  public void setConfidence(Integer confidence) {
    this.confidence = confidence;
  }

  public Integer getNumericScore() {
    return numericScore;
  }

  public void setNumericScore(Integer numericScore) {
    this.numericScore = numericScore;
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
    private Long assignmentId;
    private Long submissionId;
    private Long reviewerId;
    private String reviewerName;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String comments;
    private String score;
    private String status;
    private Boolean isConfidential;
    private Integer overallRating;
    private Integer confidence;
    private Integer numericScore;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder assignmentId(Long assignmentId) {
      this.assignmentId = assignmentId;
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

    public Builder summary(String summary) {
      this.summary = summary;
      return this;
    }

    public Builder strengths(String strengths) {
      this.strengths = strengths;
      return this;
    }

    public Builder weaknesses(String weaknesses) {
      this.weaknesses = weaknesses;
      return this;
    }

    public Builder comments(String comments) {
      this.comments = comments;
      return this;
    }

    public Builder score(String score) {
      this.score = score;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder isConfidential(Boolean isConfidential) {
      this.isConfidential = isConfidential;
      return this;
    }

    public Builder overallRating(Integer overallRating) {
      this.overallRating = overallRating;
      return this;
    }

    public Builder confidence(Integer confidence) {
      this.confidence = confidence;
      return this;
    }

    public Builder numericScore(Integer numericScore) {
      this.numericScore = numericScore;
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

    public ReviewResponseDTO build() {
      return new ReviewResponseDTO(
          id,
          assignmentId,
          submissionId,
          reviewerId,
          reviewerName,
          summary,
          strengths,
          weaknesses,
          comments,
          score,
          status,
          isConfidential,
          overallRating,
          confidence,
          numericScore,
          createdAt,
          submittedAt);
    }
  }
}
