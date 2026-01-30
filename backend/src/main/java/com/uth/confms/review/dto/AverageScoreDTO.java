package com.uth.confms.review.dto;

public class AverageScoreDTO {
  private Long submissionId;
  private Double averageScore; // Điểm trung bình
  private Integer reviewCount; // Số lượng review

  public AverageScoreDTO() {
  }

  public AverageScoreDTO(Long submissionId, Double averageScore, Integer reviewCount) {
    this.submissionId = submissionId;
    this.averageScore = averageScore;
    this.reviewCount = reviewCount;
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
}
