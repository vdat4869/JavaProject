package com.uth.confms.assignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request auto-assign reviewers
 *
 * <p>DTO này được sử dụng khi chair muốn tự động assign reviewers cho submission dựa trên
 * suggestions.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class AutoAssignRequestDTO {
  @NotNull(message = "Submission ID is required")
  private Long submissionId;

  @Min(value = 1, message = "Number of reviewers must be at least 1")
  private Integer numberOfReviewers = 3; // Default: 3 reviewers

  public AutoAssignRequestDTO() {}

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public Integer getNumberOfReviewers() {
    return numberOfReviewers;
  }

  public void setNumberOfReviewers(Integer numberOfReviewers) {
    this.numberOfReviewers = numberOfReviewers;
  }
}
