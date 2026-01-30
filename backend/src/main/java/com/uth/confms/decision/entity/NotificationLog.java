package com.uth.confms.decision.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notification_logs")
@EntityListeners(AuditingEntityListener.class)
public class NotificationLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long submissionId;

  private Long userId; // Người nhận (Author)

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type; // Loại thông báo

  private String subject; // Tiêu đề email

  @Column(columnDefinition = "TEXT")
  private String content; // Nội dung email

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime sentAt; // Thời gian gửi

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationStatus status = NotificationStatus.SENT; // Trạng thái gửi

  public NotificationLog() {
  }

  public NotificationLog(
      Long id,
      Long submissionId,
      Long userId,
      NotificationType type,
      String subject,
      String content,
      LocalDateTime sentAt,
      NotificationStatus status) {
    this.id = id;
    this.submissionId = submissionId;
    this.userId = userId;
    this.type = type;
    this.subject = subject;
    this.content = content;
    this.sentAt = sentAt;
    this.status = status;
  }

  public static Builder builder() {
    return new Builder();
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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getSentAt() {
    return sentAt;
  }

  public void setSentAt(LocalDateTime sentAt) {
    this.sentAt = sentAt;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public void setStatus(NotificationStatus status) {
    this.status = status;
  }

  public enum NotificationType {
    DECISION_ACCEPT,
    DECISION_REJECT,
    DECISION_CONDITIONAL_ACCEPT,
    REVIEW_REQUEST,
    DEADLINE_REMINDER
  }

  public enum NotificationStatus {
    SENT,
    FAILED,
    PENDING
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private Long userId;
    private NotificationType type;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
    private NotificationStatus status = NotificationStatus.SENT;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder type(NotificationType type) {
      this.type = type;
      return this;
    }

    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder sentAt(LocalDateTime sentAt) {
      this.sentAt = sentAt;
      return this;
    }

    public Builder status(NotificationStatus status) {
      this.status = status;
      return this;
    }

    public NotificationLog build() {
      return new NotificationLog(id, submissionId, userId, type, subject, content, sentAt, status);
    }
  }
}
