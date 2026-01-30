package com.uth.confms.decision.dto;

import java.util.Map;

/**
 * DTO chứa review summary cho submission
 *
 * <p>
 * DTO này cung cấp thông tin tổng hợp về reviews để hỗ trợ chair trong
 * decision-making:
 *
 * <ul>
 * <li>Average score - Điểm trung bình
 * <li>Review count - Số lượng reviews đã submit
 * <li>Score distribution - Phân bố điểm
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class ReviewSummaryDTO {
  private Long submissionId;
  private Double averageScore; // Điểm trung bình
  private Integer reviewCount; // Số lượng review
  private Map<String, Integer> scoreDistribution; // Phân bố điểm số

  public ReviewSummaryDTO() {
  }

  public ReviewSummaryDTO(
      Long submissionId,
      Double averageScore,
      Integer reviewCount,
      Map<String, Integer> scoreDistribution) {
    this.submissionId = submissionId;
    this.averageScore = averageScore;
    this.reviewCount = reviewCount;
    this.scoreDistribution = scoreDistribution;
  }

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public Double getAverageScore() {
    return averageScore;
  }

  public void setAverageScore(Double averageScore) {
    this.averageScore = averageScore;
  }

  public Integer getReviewCount() {
    return reviewCount;
  }

  public void setReviewCount(Integer reviewCount) {
    this.reviewCount = reviewCount;
  }

  public Map<String, Integer> getScoreDistribution() {
    return scoreDistribution;
  }

  public void setScoreDistribution(Map<String, Integer> scoreDistribution) {
    this.scoreDistribution = scoreDistribution;
  }
}
