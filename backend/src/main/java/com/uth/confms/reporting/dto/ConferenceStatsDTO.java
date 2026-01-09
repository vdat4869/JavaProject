package com.uth.confms.reporting.dto;

public class ConferenceStatsDTO {
  private Long conferenceId;
  private String conferenceName;
  private Integer totalSubmissions;
  private Integer acceptedCount;
  private Integer rejectedCount;
  private Integer pendingCount;
  private Double acceptanceRate;
  private Integer totalReviews;
  private Integer completedReviews;
  private Double reviewCompletionRate;

  public ConferenceStatsDTO() {}

  public ConferenceStatsDTO(
      Long conferenceId,
      String conferenceName,
      Integer totalSubmissions,
      Integer acceptedCount,
      Integer rejectedCount,
      Integer pendingCount,
      Double acceptanceRate,
      Integer totalReviews,
      Integer completedReviews,
      Double reviewCompletionRate) {
    this.conferenceId = conferenceId;
    this.conferenceName = conferenceName;
    this.totalSubmissions = totalSubmissions;
    this.acceptedCount = acceptedCount;
    this.rejectedCount = rejectedCount;
    this.pendingCount = pendingCount;
    this.acceptanceRate = acceptanceRate;
    this.totalReviews = totalReviews;
    this.completedReviews = completedReviews;
    this.reviewCompletionRate = reviewCompletionRate;
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

  public String getConferenceName() {
    return conferenceName;
  }

  public void setConferenceName(String conferenceName) {
    this.conferenceName = conferenceName;
  }

  public Integer getTotalSubmissions() {
    return totalSubmissions;
  }

  public void setTotalSubmissions(Integer totalSubmissions) {
    this.totalSubmissions = totalSubmissions;
  }

  public Integer getAcceptedCount() {
    return acceptedCount;
  }

  public void setAcceptedCount(Integer acceptedCount) {
    this.acceptedCount = acceptedCount;
  }

  public Integer getRejectedCount() {
    return rejectedCount;
  }

  public void setRejectedCount(Integer rejectedCount) {
    this.rejectedCount = rejectedCount;
  }

  public Integer getPendingCount() {
    return pendingCount;
  }

  public void setPendingCount(Integer pendingCount) {
    this.pendingCount = pendingCount;
  }

  public Double getAcceptanceRate() {
    return acceptanceRate;
  }

  public void setAcceptanceRate(Double acceptanceRate) {
    this.acceptanceRate = acceptanceRate;
  }

  public Integer getTotalReviews() {
    return totalReviews;
  }

  public void setTotalReviews(Integer totalReviews) {
    this.totalReviews = totalReviews;
  }

  public Integer getCompletedReviews() {
    return completedReviews;
  }

  public void setCompletedReviews(Integer completedReviews) {
    this.completedReviews = completedReviews;
  }

  public Double getReviewCompletionRate() {
    return reviewCompletionRate;
  }

  public void setReviewCompletionRate(Double reviewCompletionRate) {
    this.reviewCompletionRate = reviewCompletionRate;
  }

  public static class Builder {
    private Long conferenceId;
    private String conferenceName;
    private Integer totalSubmissions;
    private Integer acceptedCount;
    private Integer rejectedCount;
    private Integer pendingCount;
    private Double acceptanceRate;
    private Integer totalReviews;
    private Integer completedReviews;
    private Double reviewCompletionRate;

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder conferenceName(String conferenceName) {
      this.conferenceName = conferenceName;
      return this;
    }

    public Builder totalSubmissions(Integer totalSubmissions) {
      this.totalSubmissions = totalSubmissions;
      return this;
    }

    public Builder acceptedCount(Integer acceptedCount) {
      this.acceptedCount = acceptedCount;
      return this;
    }

    public Builder rejectedCount(Integer rejectedCount) {
      this.rejectedCount = rejectedCount;
      return this;
    }

    public Builder pendingCount(Integer pendingCount) {
      this.pendingCount = pendingCount;
      return this;
    }

    public Builder acceptanceRate(Double acceptanceRate) {
      this.acceptanceRate = acceptanceRate;
      return this;
    }

    public Builder totalReviews(Integer totalReviews) {
      this.totalReviews = totalReviews;
      return this;
    }

    public Builder completedReviews(Integer completedReviews) {
      this.completedReviews = completedReviews;
      return this;
    }

    public Builder reviewCompletionRate(Double reviewCompletionRate) {
      this.reviewCompletionRate = reviewCompletionRate;
      return this;
    }

    public ConferenceStatsDTO build() {
      return new ConferenceStatsDTO(
          conferenceId,
          conferenceName,
          totalSubmissions,
          acceptedCount,
          rejectedCount,
          pendingCount,
          acceptanceRate,
          totalReviews,
          completedReviews,
          reviewCompletionRate);
    }
  }
}
