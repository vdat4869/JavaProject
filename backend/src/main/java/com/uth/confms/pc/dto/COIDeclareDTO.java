package com.uth.confms.pc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class COIDeclareDTO {
  @NotNull(message = "Submission ID is required")
  private Long submissionId;

  @NotBlank(message = "COI type is required")
  private String type;

  private String reason;

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
