package com.uth.confms.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RebuttalSubmitDTO {
  @NotNull(message = "Submission ID is required")
  private Long submissionId;

  @NotBlank(message = "Content is required")
  private String content; // Nội dung phản biện

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
