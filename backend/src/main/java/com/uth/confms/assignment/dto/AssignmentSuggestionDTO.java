package com.uth.confms.assignment.dto;

public class AssignmentSuggestionDTO {
  private Long reviewerId;
  private String reviewerEmail;
  private String reviewerName;
  private Double score;
  private String reason;
  private Boolean hasCOI;

  public AssignmentSuggestionDTO() {}

  public AssignmentSuggestionDTO(
      Long reviewerId,
      String reviewerEmail,
      String reviewerName,
      Double score,
      String reason,
      Boolean hasCOI) {
    this.reviewerId = reviewerId;
    this.reviewerEmail = reviewerEmail;
    this.reviewerName = reviewerName;
    this.score = score;
    this.reason = reason;
    this.hasCOI = hasCOI;
  }

  public Long getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(Long reviewerId) {
    this.reviewerId = reviewerId;
  }

  public String getReviewerEmail() {
    return reviewerEmail;
  }

  public void setReviewerEmail(String reviewerEmail) {
    this.reviewerEmail = reviewerEmail;
  }

  public String getReviewerName() {
    return reviewerName;
  }

  public void setReviewerName(String reviewerName) {
    this.reviewerName = reviewerName;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Boolean getHasCOI() {
    return hasCOI;
  }

  public void setHasCOI(Boolean hasCOI) {
    this.hasCOI = hasCOI;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long reviewerId;
    private String reviewerEmail;
    private String reviewerName;
    private Double score;
    private String reason;
    private Boolean hasCOI;

    public Builder reviewerId(Long reviewerId) {
      this.reviewerId = reviewerId;
      return this;
    }

    public Builder reviewerEmail(String reviewerEmail) {
      this.reviewerEmail = reviewerEmail;
      return this;
    }

    public Builder reviewerName(String reviewerName) {
      this.reviewerName = reviewerName;
      return this;
    }

    public Builder score(Double score) {
      this.score = score;
      return this;
    }

    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    public Builder hasCOI(Boolean hasCOI) {
      this.hasCOI = hasCOI;
      return this;
    }

    public AssignmentSuggestionDTO build() {
      return new AssignmentSuggestionDTO(
          reviewerId, reviewerEmail, reviewerName, score, reason, hasCOI);
    }
  }
}
