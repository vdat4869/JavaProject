package com.uth.confms.reporting.dto;

public class ReviewStatsDTO {
  private Long conferenceId;
  private Integer totalAssignments;
  private Integer completedReviews;
  private Integer pendingReviews;
  private Double completionRate;
  private Integer averageReviewTime; // in hours

  public ReviewStatsDTO() {}

  public ReviewStatsDTO(
      Long conferenceId,
      Integer totalAssignments,
      Integer completedReviews,
      Integer pendingReviews,
      Double completionRate,
      Integer averageReviewTime) {
    this.conferenceId = conferenceId;
    this.totalAssignments = totalAssignments;
    this.completedReviews = completedReviews;
    this.pendingReviews = pendingReviews;
    this.completionRate = completionRate;
    this.averageReviewTime = averageReviewTime;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public Integer getTotalAssignments() {
    return totalAssignments;
  }

  public void setTotalAssignments(Integer totalAssignments) {
    this.totalAssignments = totalAssignments;
  }

  public Integer getCompletedReviews() {
    return completedReviews;
  }

  public void setCompletedReviews(Integer completedReviews) {
    this.completedReviews = completedReviews;
  }

  public Integer getPendingReviews() {
    return pendingReviews;
  }

  public void setPendingReviews(Integer pendingReviews) {
    this.pendingReviews = pendingReviews;
  }

  public Double getCompletionRate() {
    return completionRate;
  }

  public void setCompletionRate(Double completionRate) {
    this.completionRate = completionRate;
  }

  public Integer getAverageReviewTime() {
    return averageReviewTime;
  }

  public void setAverageReviewTime(Integer averageReviewTime) {
    this.averageReviewTime = averageReviewTime;
  }

  public static class Builder {
    private Long conferenceId;
    private Integer totalAssignments;
    private Integer completedReviews;
    private Integer pendingReviews;
    private Double completionRate;
    private Integer averageReviewTime;

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder totalAssignments(Integer totalAssignments) {
      this.totalAssignments = totalAssignments;
      return this;
    }

    public Builder completedReviews(Integer completedReviews) {
      this.completedReviews = completedReviews;
      return this;
    }

    public Builder pendingReviews(Integer pendingReviews) {
      this.pendingReviews = pendingReviews;
      return this;
    }

    public Builder completionRate(Double completionRate) {
      this.completionRate = completionRate;
      return this;
    }

    public Builder averageReviewTime(Integer averageReviewTime) {
      this.averageReviewTime = averageReviewTime;
      return this;
    }

    public ReviewStatsDTO build() {
      return new ReviewStatsDTO(
          conferenceId, totalAssignments, completedReviews, pendingReviews, completionRate, averageReviewTime);
    }
  }
}
