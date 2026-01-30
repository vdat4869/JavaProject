package com.uth.confms.decision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request tạo decision
 *
 * <p>
 * DTO này chứa thông tin decision cần tạo:
 *
 * <ul>
 * <li>submissionId - ID của submission (required)
 * <li>type - Loại decision (required): ACCEPT, REJECT, CONDITIONAL_ACCEPT
 * <li>comments - Comments của chair (optional)
 * <li>sendNotification - Có gửi email notification ngay không (default: true)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class DecisionRequestDTO {
  @NotNull(message = "Submission ID is required")
  private Long submissionId;

  @NotBlank(message = "Decision type is required")
  private String type; // ACCEPT, REJECT, CONDITIONAL_ACCEPT

  private String comments; // Nhận xét

  private Boolean sendNotification = true; // Gửi email ngay lập tức (mặc định: true)

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

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Boolean getSendNotification() {
    return sendNotification;
  }

  public void setSendNotification(Boolean sendNotification) {
    this.sendNotification = sendNotification;
  }
}
