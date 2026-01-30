package com.uth.confms.review.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ReviewStatisticsDTO {
  private Long conferenceId;
  private Double completionRate; // Tỷ lệ hoàn thành review
  private Double averageScore; // Điểm trung bình của tất cả submission
  private Map<String, Integer> scoreDistribution; // Phân bố điểm số
  private Double averageCompletionTime; // Thời gian trung bình hoàn thành review (ngày)
  private Integer totalReviews; // Tổng số review
  private Integer completedReviews; // Review đã hoàn thành
  private Integer pendingReviews; // Review đang chờ
  private Map<LocalDateTime, Integer> submissionTimeline; // Review nộp theo ngày
  private Map<Long, ReviewerPerformanceDTO> reviewerMetrics; // Hiệu suất reviewer

  public ReviewStatisticsDTO() {
  }

  public ReviewStatisticsDTO(
      Long conferenceId,
      Double completionRate,
      Double averageScore,
      Map<String, Integer> scoreDistribution,
      Double averageCompletionTime,
      Integer totalReviews,
      Integer completedReviews,
      Integer pendingReviews,
      Map<LocalDateTime, Integer> submissionTimeline,
      Map<Long, ReviewerPerformanceDTO> reviewerMetrics) {
    this.conferenceId = conferenceId;
    this.completionRate = completionRate;
    this.averageScore = averageScore;
    this.scoreDistribution = scoreDistribution;
    this.averageCompletionTime = averageCompletionTime;
    this.totalReviews = totalReviews;
    this.completedReviews = completedReviews;
    this.pendingReviews = pendingReviews;
    this.submissionTimeline = submissionTimeline;
    this.reviewerMetrics = reviewerMetrics;
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public Double getCompletionRate() {
    return completionRate;
  }

  public void setCompletionRate(Double completionRate) {
    this.completionRate = completionRate;
  }

  public Double getAverageScore() {
    return averageScore;
  }

  public void setAverageScore(Double averageScore) {
    this.averageScore = averageScore;
  }

  public Map<String, Integer> getScoreDistribution() {
    return scoreDistribution;
  }

  public void setScoreDistribution(Map<String, Integer> scoreDistribution) {
    this.scoreDistribution = scoreDistribution;
  }

  public Double getAverageCompletionTime() {
    return averageCompletionTime;
  }

  public void setAverageCompletionTime(Double averageCompletionTime) {
    this.averageCompletionTime = averageCompletionTime;
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

  public Map<LocalDateTime, Integer> getSubmissionTimeline() {
    return submissionTimeline;
  }

  public void setSubmissionTimeline(Map<LocalDateTime, Integer> submissionTimeline) {
    this.submissionTimeline = submissionTimeline;
  }

  public Map<Long, ReviewerPerformanceDTO> getReviewerMetrics() {
    return reviewerMetrics;
  }

  public void setReviewerMetrics(Map<Long, ReviewerPerformanceDTO> reviewerMetrics) {
    this.reviewerMetrics = reviewerMetrics;
  }
}
