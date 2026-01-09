package com.uth.confms.reporting.dto;

import java.time.LocalDateTime;

public class ReportResponseDTO {
  private Long id;
  private Long conferenceId;
  private Integer totalSubmissions;
  private Integer acceptedCount;
  private Integer rejectedCount;
  private Integer pendingCount;
  private Double acceptanceRate;
  private Integer totalReviews;
  private Integer completedReviews;
  private Integer pendingReviews;
  private Integer totalAssignments;
  private Integer acceptedAssignments;
  private Integer declinedAssignments;
  private LocalDateTime snapshotAt;

  public ReportResponseDTO() {}

  public ReportResponseDTO(
      Long id,
      Long conferenceId,
      Integer totalSubmissions,
      Integer acceptedCount,
      Integer rejectedCount,
      Integer pendingCount,
      Double acceptanceRate,
      Integer totalReviews,
      Integer completedReviews,
      Integer pendingReviews,
      Integer totalAssignments,
      Integer acceptedAssignments,
      Integer declinedAssignments,
      LocalDateTime snapshotAt) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.totalSubmissions = totalSubmissions;
    this.acceptedCount = acceptedCount;
    this.rejectedCount = rejectedCount;
    this.pendingCount = pendingCount;
    this.acceptanceRate = acceptanceRate;
    this.totalReviews = totalReviews;
    this.completedReviews = completedReviews;
    this.pendingReviews = pendingReviews;
    this.totalAssignments = totalAssignments;
    this.acceptedAssignments = acceptedAssignments;
    this.declinedAssignments = declinedAssignments;
    this.snapshotAt = snapshotAt;
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

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
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

  public Integer getPendingReviews() {
    return pendingReviews;
  }

  public void setPendingReviews(Integer pendingReviews) {
    this.pendingReviews = pendingReviews;
  }

  public Integer getTotalAssignments() {
    return totalAssignments;
  }

  public void setTotalAssignments(Integer totalAssignments) {
    this.totalAssignments = totalAssignments;
  }

  public Integer getAcceptedAssignments() {
    return acceptedAssignments;
  }

  public void setAcceptedAssignments(Integer acceptedAssignments) {
    this.acceptedAssignments = acceptedAssignments;
  }

  public Integer getDeclinedAssignments() {
    return declinedAssignments;
  }

  public void setDeclinedAssignments(Integer declinedAssignments) {
    this.declinedAssignments = declinedAssignments;
  }

  public LocalDateTime getSnapshotAt() {
    return snapshotAt;
  }

  public void setSnapshotAt(LocalDateTime snapshotAt) {
    this.snapshotAt = snapshotAt;
  }

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private Integer totalSubmissions;
    private Integer acceptedCount;
    private Integer rejectedCount;
    private Integer pendingCount;
    private Double acceptanceRate;
    private Integer totalReviews;
    private Integer completedReviews;
    private Integer pendingReviews;
    private Integer totalAssignments;
    private Integer acceptedAssignments;
    private Integer declinedAssignments;
    private LocalDateTime snapshotAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
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

    public Builder pendingReviews(Integer pendingReviews) {
      this.pendingReviews = pendingReviews;
      return this;
    }

    public Builder totalAssignments(Integer totalAssignments) {
      this.totalAssignments = totalAssignments;
      return this;
    }

    public Builder acceptedAssignments(Integer acceptedAssignments) {
      this.acceptedAssignments = acceptedAssignments;
      return this;
    }

    public Builder declinedAssignments(Integer declinedAssignments) {
      this.declinedAssignments = declinedAssignments;
      return this;
    }

    public Builder snapshotAt(LocalDateTime snapshotAt) {
      this.snapshotAt = snapshotAt;
      return this;
    }

    public ReportResponseDTO build() {
      return new ReportResponseDTO(
          id,
          conferenceId,
          totalSubmissions,
          acceptedCount,
          rejectedCount,
          pendingCount,
          acceptanceRate,
          totalReviews,
          completedReviews,
          pendingReviews,
          totalAssignments,
          acceptedAssignments,
          declinedAssignments,
          snapshotAt);
    }
  }
}
