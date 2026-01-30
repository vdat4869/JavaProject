package com.uth.confms.decision.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho lịch sử thay đổi của decision (audit trail)
 *
 * <p>
 * DecisionHistory track mọi thay đổi của decision để đảm bảo audit trail đầy
 * đủ:
 *
 * <ul>
 * <li>Track changes: type, comments, locked status
 * <li>Track who made the change và when
 * <li>Support compliance và audit requirements
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "decision_history")
@EntityListeners(AuditingEntityListener.class)
public class DecisionHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long decisionId;

  @Column(nullable = false)
  private Long changedBy; // Người thực hiện thay đổi

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ChangeType changeType; // Loại thay đổi

  @Column(columnDefinition = "TEXT")
  private String oldValue; // Giá trị cũ

  @Column(columnDefinition = "TEXT")
  private String newValue; // Giá trị mới

  @Column(columnDefinition = "TEXT")
  private String fieldName; // Tên trường bị thay đổi

  @Column(columnDefinition = "TEXT")
  private String description; // Mô tả chi tiết

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime changedAt;

  public DecisionHistory() {
  }

  public DecisionHistory(
      Long id,
      Long decisionId,
      Long changedBy,
      ChangeType changeType,
      String oldValue,
      String newValue,
      String fieldName,
      String description,
      LocalDateTime changedAt) {
    this.id = id;
    this.decisionId = decisionId;
    this.changedBy = changedBy;
    this.changeType = changeType;
    this.oldValue = oldValue;
    this.newValue = newValue;
    this.fieldName = fieldName;
    this.description = description;
    this.changedAt = changedAt;
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

  public Long getDecisionId() {
    return decisionId;
  }

  public void setDecisionId(Long decisionId) {
    this.decisionId = decisionId;
  }

  public Long getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(Long changedBy) {
    this.changedBy = changedBy;
  }

  public ChangeType getChangeType() {
    return changeType;
  }

  public void setChangeType(ChangeType changeType) {
    this.changeType = changeType;
  }

  public String getOldValue() {
    return oldValue;
  }

  public void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getChangedAt() {
    return changedAt;
  }

  public void setChangedAt(LocalDateTime changedAt) {
    this.changedAt = changedAt;
  }

  /** Enum định nghĩa các loại thay đổi */
  public enum ChangeType {
    /** Decision được tạo mới */
    CREATED,
    /** Decision type được thay đổi */
    TYPE_CHANGED,
    /** Comments được thay đổi */
    COMMENTS_CHANGED,
    /** Decision được lock */
    LOCKED,
    /** Decision được unlock (nếu có) */
    UNLOCKED,
    /** Notification status được thay đổi */
    NOTIFICATION_STATUS_CHANGED
  }

  public static class Builder {
    private Long id;
    private Long decisionId;
    private Long changedBy;
    private ChangeType changeType;
    private String oldValue;
    private String newValue;
    private String fieldName;
    private String description;
    private LocalDateTime changedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder decisionId(Long decisionId) {
      this.decisionId = decisionId;
      return this;
    }

    public Builder changedBy(Long changedBy) {
      this.changedBy = changedBy;
      return this;
    }

    public Builder changeType(ChangeType changeType) {
      this.changeType = changeType;
      return this;
    }

    public Builder oldValue(String oldValue) {
      this.oldValue = oldValue;
      return this;
    }

    public Builder newValue(String newValue) {
      this.newValue = newValue;
      return this;
    }

    public Builder fieldName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder changedAt(LocalDateTime changedAt) {
      this.changedAt = changedAt;
      return this;
    }

    public DecisionHistory build() {
      return new DecisionHistory(
          id, decisionId, changedBy, changeType, oldValue, newValue, fieldName, description, changedAt);
    }
  }
}
