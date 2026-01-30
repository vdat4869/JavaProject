package com.uth.confms.assignment.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public class AssignmentResponseDTO {
  private Long id;
  private Long submissionId;
  private String submissionTitle;
  private Long reviewerId;
  private String reviewerEmail;
  private String reviewerName;
  private String status; // Trạng thái assignment
  private Boolean isPrimary; // Có phải là reviewer chính không
  private String submissionAbstract; // Tóm tắt bài báo (cho reviewer xem)
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime assignedAt; // Thời điểm phân công
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime updatedAt; // Thời điểm cập nhật cuối
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime deadline; // Hạn chót nộp review

  public AssignmentResponseDTO() {
  }

  public AssignmentResponseDTO(
      Long id,
      Long submissionId,
      String submissionTitle,
      Long reviewerId,
      String reviewerEmail,
      String reviewerName,
      String status,
      Boolean isPrimary,
      String submissionAbstract,
      LocalDateTime assignedAt,
      LocalDateTime updatedAt,
      LocalDateTime deadline) { // Added deadline parameter
    this.id = id;
    this.submissionId = submissionId;
    this.submissionTitle = submissionTitle;
    this.reviewerId = reviewerId;
    this.reviewerEmail = reviewerEmail;
    this.reviewerName = reviewerName;
    this.status = status;
    this.isPrimary = isPrimary;
    this.submissionAbstract = submissionAbstract;
    this.assignedAt = assignedAt;
    this.updatedAt = updatedAt;
    this.deadline = deadline;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public String getSubmissionTitle() {
    return submissionTitle;
  }

  public void setSubmissionTitle(String submissionTitle) {
    this.submissionTitle = submissionTitle;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  public String getSubmissionAbstract() {
    return submissionAbstract;
  }

  public void setSubmissionAbstract(String submissionAbstract) {
    this.submissionAbstract = submissionAbstract;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(LocalDateTime assignedAt) {
    this.assignedAt = assignedAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDateTime deadline) {
    this.deadline = deadline;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private String submissionTitle;
    private Long reviewerId;
    private String reviewerEmail;
    private String reviewerName;
    private String status;
    private Boolean isPrimary;
    private String submissionAbstract;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deadline; // Added deadline

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder submissionTitle(String submissionTitle) {
      this.submissionTitle = submissionTitle;
      return this;
    }

    public Builder reviewerId(Long reviewerId) {
      this.reviewerId = reviewerId;
      return this;
    }

    public Builder reviewerEmail(String reviewerEmail) {
      this.reviewerEmail = reviewerEmail;
      return this;
    }

    public Builder reviewerName(String reviewerName) {
      this.reviewerName = reviewerName;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder isPrimary(Boolean isPrimary) {
      this.isPrimary = isPrimary;
      return this;
    }

    public Builder submissionAbstract(String submissionAbstract) {
      this.submissionAbstract = submissionAbstract;
      return this;
    }

    public Builder assignedAt(LocalDateTime assignedAt) {
      this.assignedAt = assignedAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public Builder deadline(LocalDateTime deadline) { // Added deadline
      this.deadline = deadline;
      return this;
    }

    public AssignmentResponseDTO build() {
      return new AssignmentResponseDTO(
          id,
          submissionId,
          submissionTitle,
          reviewerId,
          reviewerEmail,
          reviewerName,
          status,
          isPrimary,
          submissionAbstract,
          assignedAt,
          updatedAt,
          deadline); // Pass deadline to constructor
    }
  }
}
