package com.uth.confms.decision.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BulkNotificationRequestDTO {
  @NotEmpty(message = "Submission IDs are required")
  private List<Long> submissionIds;

  @NotNull(message = "Notification type is required")
  private String notificationType; // DECISION_ACCEPT, DECISION_REJECT, etc.

  private String customSubject;

  private String customMessage;

  public List<Long> getSubmissionIds() {
    return submissionIds;
  }

  public void setSubmissionIds(List<Long> submissionIds) {
    this.submissionIds = submissionIds;
  }

  public String getNotificationType() {
    return notificationType;
  }

  public void setNotificationType(String notificationType) {
    this.notificationType = notificationType;
  }

  public String getCustomSubject() {
    return customSubject;
  }

  public void setCustomSubject(String customSubject) {
    this.customSubject = customSubject;
  }

  public String getCustomMessage() {
    return customMessage;
  }

  public void setCustomMessage(String customMessage) {
    this.customMessage = customMessage;
  }
}
