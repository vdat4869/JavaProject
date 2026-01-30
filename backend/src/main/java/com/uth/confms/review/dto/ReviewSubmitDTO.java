package com.uth.confms.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request submit review
 *
 * <p>
 * DTO này chứa thông tin review cần submit:
 *
 * <ul>
 * <li>assignmentId - ID của assignment (required)
 * <li>summary - Tóm tắt review (required)
 * <li>strengths - Điểm mạnh (optional)
 * <li>weaknesses - Điểm yếu (optional)
 * <li>comments - Comments chi tiết (required)
 * <li>score - Điểm đánh giá (required): STRONG_ACCEPT, ACCEPT, WEAK_ACCEPT,
 * BORDERLINE,
 * WEAK_REJECT, REJECT, STRONG_REJECT
 * <li>isConfidential - Có phải confidential review không (required)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class ReviewSubmitDTO {
  @NotNull(message = "Assignment ID is required")
  private Long assignmentId;

  @NotBlank(message = "Summary is required")
  private String summary; // Tóm tắt

  private String strengths; // Điểm mạnh

  private String weaknesses; // Điểm yếu

  @NotBlank(message = "Comments are required")
  private String comments; // Nhận xét

  @NotBlank(message = "Score is required")
  private String score; // Điểm đánh giá (enum)

  @NotNull(message = "Is confidential flag is required")
  private Boolean isConfidential; // Bí mật

  private Integer overallRating; // Đánh giá chung (1-5)

  private Integer confidence; // Độ tự tin (1-5)

  private Long templateId; // Optional: ID mẫu review để áp dụng

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
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

  public Long getTemplateId() {
    return templateId;
  }

  public void setTemplateId(Long templateId) {
    this.templateId = templateId;
  }
}
