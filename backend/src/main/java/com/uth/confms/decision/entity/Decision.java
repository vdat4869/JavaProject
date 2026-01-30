package com.uth.confms.decision.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho decision (quyết định) của chair về submission
 *
 * <p>
 * Decision được tạo bởi chair sau khi có đủ reviews. Decision có các loại:
 *
 * <ul>
 * <li>ACCEPT - Chấp nhận submission
 * <li>REJECT - Từ chối submission
 * <li>CONDITIONAL_ACCEPT - Chấp nhận có điều kiện
 * </ul>
 *
 * <p>
 * Decision có thể được notify (gửi email) cho author hoặc chưa.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "decisions")
@EntityListeners(AuditingEntityListener.class)
public class Decision {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long submissionId; // ID bài báo

  @Column(nullable = false)
  private Long decidedBy; // ID người ra quyết định (Chair)

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DecisionType type; // Loại quyết định (Accept, Reject...)

  @Column(columnDefinition = "TEXT")
  private String comments; // Nhận xét của Chair

  @Column(nullable = false)
  private Boolean notified = false; // Đã gửi thông báo cho tác giả chưa

  @Column(nullable = false)
  private Boolean locked = false; // Đã khóa quyết định chưa (sau khi notify)

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime decidedAt;

  public Decision() {
  }

  public Decision(
      Long id,
      Long submissionId,
      Long decidedBy,
      DecisionType type,
      String comments,
      Boolean notified,
      Boolean locked,
      LocalDateTime decidedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.decidedBy = decidedBy;
    this.type = type;
    this.comments = comments;
    this.notified = notified;
    this.locked = locked;
    this.decidedAt = decidedAt;
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

  public Long getDecidedBy() {
    return decidedBy;
  }

  public void setDecidedBy(Long decidedBy) {
    this.decidedBy = decidedBy;
  }

  public DecisionType getType() {
    return type;
  }

  public void setType(DecisionType type) {
    this.type = type;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Boolean getNotified() {
    return notified;
  }

  public void setNotified(Boolean notified) {
    this.notified = notified;
  }

  public LocalDateTime getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(LocalDateTime decidedAt) {
    this.decidedAt = decidedAt;
  }

  public Boolean getLocked() {
    return locked;
  }

  public void setLocked(Boolean locked) {
    this.locked = locked;
  }

  /** Enum định nghĩa các loại decision */
  public enum DecisionType {
    /** Chấp nhận submission */
    ACCEPT,
    /** Từ chối submission */
    REJECT,
    /** Chấp nhận có điều kiện */
    CONDITIONAL_ACCEPT
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private Long decidedBy;
    private DecisionType type;
    private String comments;
    private Boolean notified = false;
    private Boolean locked = false;
    private LocalDateTime decidedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder decidedBy(Long decidedBy) {
      this.decidedBy = decidedBy;
      return this;
    }

    public Builder type(DecisionType type) {
      this.type = type;
      return this;
    }

    public Builder comments(String comments) {
      this.comments = comments;
      return this;
    }

    public Builder notified(Boolean notified) {
      this.notified = notified;
      return this;
    }

    public Builder locked(Boolean locked) {
      this.locked = locked;
      return this;
    }

    public Builder decidedAt(LocalDateTime decidedAt) {
      this.decidedAt = decidedAt;
      return this;
    }

    public Decision build() {
      return new Decision(id, submissionId, decidedBy, type, comments, notified, locked, decidedAt);
    }
  }
}
