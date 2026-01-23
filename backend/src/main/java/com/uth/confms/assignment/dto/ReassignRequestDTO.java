package com.uth.confms.assignment.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request reassign reviewer
 *
 * <p>DTO này được sử dụng khi chair muốn reassign một assignment từ reviewer cũ sang reviewer mới.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class ReassignRequestDTO {
  @NotNull(message = "New reviewer ID is required")
  private Long newReviewerId;

  private String reason; // Optional reason for reassignment

  public ReassignRequestDTO() {}

  public Long getNewReviewerId() {
    return newReviewerId;
  }

  public void setNewReviewerId(Long newReviewerId) {
    this.newReviewerId = newReviewerId;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
