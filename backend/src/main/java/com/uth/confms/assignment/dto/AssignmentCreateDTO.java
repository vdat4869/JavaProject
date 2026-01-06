package com.uth.confms.assignment.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request tạo assignment mới
 *
 * <p>DTO này được sử dụng khi chair muốn assign một reviewer cho submission.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class AssignmentCreateDTO {
  @NotNull(message = "Submission ID is required")
  private Long submissionId;

  @NotNull(message = "Reviewer ID is required")
  private Long reviewerId;

  private Boolean isPrimary = false;

  public AssignmentCreateDTO() {}

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public Long getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(Long reviewerId) {
    this.reviewerId = reviewerId;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }
}
