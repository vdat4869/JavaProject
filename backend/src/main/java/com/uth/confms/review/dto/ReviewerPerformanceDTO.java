package com.uth.confms.review.dto;

public class ReviewerPerformanceDTO {
  private Long reviewerId;
  private String reviewerName; // Tên reviewer
  private Integer totalReviews; // Tổng số review được giao
  private Integer completedReviews; // Review đã hoàn thành
  private Double averageScore; // Điểm trung bình đã cho
  private Double averageCompletionTime; // Thời gian hoàn thành trung bình (ngày)
  private Double completionRate; // Tỷ lệ hoàn thành

  public ReviewerPerformanceDTO() {
  }

  public ReviewerPerformanceDTO(
      Long reviewerId,
      String reviewerName,
      Integer totalReviews,
      Integer completedReviews,
      Double averageScore,
      Double averageCompletionTime,
      Double completionRate) {
    this.reviewerId = reviewerId;
    this.reviewerName = reviewerName;
    this.totalReviews = totalReviews;
    this.completedReviews = completedReviews;
    this.averageScore = averageScore;
    this.averageCompletionTime = averageCompletionTime;
    this.completionRate = completionRate;
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

  public Double getAverageScore() {
    return averageScore;
  }

  public void setAverageScore(Double averageScore) {
    this.averageScore = averageScore;
  }

  public Double getAverageCompletionTime() {
    return averageCompletionTime;
  }

  public void setAverageCompletionTime(Double averageCompletionTime) {
    this.averageCompletionTime = averageCompletionTime;
  }

  public Double getCompletionRate() {
    return completionRate;
  }

  public void setCompletionRate(Double completionRate) {
    this.completionRate = completionRate;
  }
}
