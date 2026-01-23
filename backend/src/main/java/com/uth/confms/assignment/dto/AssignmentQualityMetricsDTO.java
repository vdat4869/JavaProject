package com.uth.confms.assignment.dto;

import java.util.Map;

/**
 * DTO cho assignment quality metrics
 *
 * <p>DTO này chứa các metrics về chất lượng assignments và reviews.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class AssignmentQualityMetricsDTO {
  private Double averageReviewScore; // Average review score (0.0 - 7.0)
  private Map<String, Integer> reviewScoreDistribution; // Distribution of review scores
  private Double averageReviewCompletionTime; // Average time to complete review (in days)
  private Integer totalReviewsSubmitted;
  private Integer totalReviewsPending;
  private Double reviewSubmissionRate; // Percentage of assignments with submitted reviews
  private Double averageReviewerRating; // Average rating of reviewers (based on review quality)

  public AssignmentQualityMetricsDTO() {}

  public AssignmentQualityMetricsDTO(
      Double averageReviewScore,
      Map<String, Integer> reviewScoreDistribution,
      Double averageReviewCompletionTime,
      Integer totalReviewsSubmitted,
      Integer totalReviewsPending,
      Double reviewSubmissionRate,
      Double averageReviewerRating) {
    this.averageReviewScore = averageReviewScore;
    this.reviewScoreDistribution = reviewScoreDistribution;
    this.averageReviewCompletionTime = averageReviewCompletionTime;
    this.totalReviewsSubmitted = totalReviewsSubmitted;
    this.totalReviewsPending = totalReviewsPending;
    this.reviewSubmissionRate = reviewSubmissionRate;
    this.averageReviewerRating = averageReviewerRating;
  }

  public Double getAverageReviewScore() {
    return averageReviewScore;
  }

  public void setAverageReviewScore(Double averageReviewScore) {
    this.averageReviewScore = averageReviewScore;
  }

  public Map<String, Integer> getReviewScoreDistribution() {
    return reviewScoreDistribution;
  }

  public void setReviewScoreDistribution(Map<String, Integer> reviewScoreDistribution) {
    this.reviewScoreDistribution = reviewScoreDistribution;
  }

  public Double getAverageReviewCompletionTime() {
    return averageReviewCompletionTime;
  }

  public void setAverageReviewCompletionTime(Double averageReviewCompletionTime) {
    this.averageReviewCompletionTime = averageReviewCompletionTime;
  }

  public Integer getTotalReviewsSubmitted() {
    return totalReviewsSubmitted;
  }

  public void setTotalReviewsSubmitted(Integer totalReviewsSubmitted) {
    this.totalReviewsSubmitted = totalReviewsSubmitted;
  }

  public Integer getTotalReviewsPending() {
    return totalReviewsPending;
  }

  public void setTotalReviewsPending(Integer totalReviewsPending) {
    this.totalReviewsPending = totalReviewsPending;
  }

  public Double getReviewSubmissionRate() {
    return reviewSubmissionRate;
  }

  public void setReviewSubmissionRate(Double reviewSubmissionRate) {
    this.reviewSubmissionRate = reviewSubmissionRate;
  }

  public Double getAverageReviewerRating() {
    return averageReviewerRating;
  }

  public void setAverageReviewerRating(Double averageReviewerRating) {
    this.averageReviewerRating = averageReviewerRating;
  }
}
