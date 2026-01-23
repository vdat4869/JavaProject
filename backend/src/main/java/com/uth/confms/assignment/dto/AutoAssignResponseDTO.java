package com.uth.confms.assignment.dto;

import java.util.List;

/**
 * DTO cho response auto-assign reviewers
 *
 * <p>DTO này chứa danh sách assignments đã tạo và failed assignments.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class AutoAssignResponseDTO {
  private List<AssignmentResponseDTO> createdAssignments;
  private List<FailedAssignmentDTO> failedAssignments;
  private Integer totalRequested;
  private Integer totalCreated;
  private Integer totalFailed;

  public AutoAssignResponseDTO() {}

  public AutoAssignResponseDTO(
      List<AssignmentResponseDTO> createdAssignments,
      List<FailedAssignmentDTO> failedAssignments,
      Integer totalRequested,
      Integer totalCreated,
      Integer totalFailed) {
    this.createdAssignments = createdAssignments;
    this.failedAssignments = failedAssignments;
    this.totalRequested = totalRequested;
    this.totalCreated = totalCreated;
    this.totalFailed = totalFailed;
  }

  public List<AssignmentResponseDTO> getCreatedAssignments() {
    return createdAssignments;
  }

  public void setCreatedAssignments(List<AssignmentResponseDTO> createdAssignments) {
    this.createdAssignments = createdAssignments;
  }

  public List<FailedAssignmentDTO> getFailedAssignments() {
    return failedAssignments;
  }

  public void setFailedAssignments(List<FailedAssignmentDTO> failedAssignments) {
    this.failedAssignments = failedAssignments;
  }

  public Integer getTotalRequested() {
    return totalRequested;
  }

  public void setTotalRequested(Integer totalRequested) {
    this.totalRequested = totalRequested;
  }

  public Integer getTotalCreated() {
    return totalCreated;
  }

  public void setTotalCreated(Integer totalCreated) {
    this.totalCreated = totalCreated;
  }

  public Integer getTotalFailed() {
    return totalFailed;
  }

  public void setTotalFailed(Integer totalFailed) {
    this.totalFailed = totalFailed;
  }

  /**
   * DTO cho failed assignment
   *
   * @author UTH-ConfMS Team
   * @version 1.0
   */
  public static class FailedAssignmentDTO {
    private Long reviewerId;
    private String reviewerEmail;
    private String reviewerName;
    private String reason;

    public FailedAssignmentDTO() {}

    public FailedAssignmentDTO(Long reviewerId, String reviewerEmail, String reviewerName, String reason) {
      this.reviewerId = reviewerId;
      this.reviewerEmail = reviewerEmail;
      this.reviewerName = reviewerName;
      this.reason = reason;
    }

    public Long getReviewerId() {
      return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
      this.reviewerId = reviewerId;
    }

    public String getReviewerEmail() {
      return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
      this.reviewerEmail = reviewerEmail;
    }

    public String getReviewerName() {
      return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
      this.reviewerName = reviewerName;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }
  }
}
