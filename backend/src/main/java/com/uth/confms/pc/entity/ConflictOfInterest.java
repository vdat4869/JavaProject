package com.uth.confms.pc.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho Conflict of Interest (COI) giữa reviewer và submission
 *
 * <p>
 * COI được khai báo bởi reviewer hoặc tự động phát hiện nếu reviewer là author.
 * COI có các loại:
 *
 * <ul>
 * <li>CO_AUTHOR - Reviewer là đồng tác giả
 * <li>COLLABORATOR - Reviewer là cộng tác viên
 * <li>ADVISOR - Reviewer là cố vấn
 * <li>INSTITUTIONAL - Cùng tổ chức
 * <li>OTHER - Lý do khác
 * </ul>
 *
 * <p>
 * Reviewer có COI sẽ không được assign để review submission đó.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "conflict_of_interests")
@EntityListeners(AuditingEntityListener.class)
public class ConflictOfInterest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long reviewerId;

  @Column(nullable = false)
  private Long submissionId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private COIType type; // Loại mâu thuẫn

  @Column(columnDefinition = "TEXT")
  private String reason; // Lý do cụ thể

  @Column(nullable = false)
  private Boolean active = true; // Still active?

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime declaredAt;

  public ConflictOfInterest() {
  }

  public ConflictOfInterest(
      Long id,
      Long reviewerId,
      Long submissionId,
      COIType type,
      String reason,
      Boolean active,
      LocalDateTime declaredAt) {
    this.id = id;
    this.reviewerId = reviewerId;
    this.submissionId = submissionId;
    this.type = type;
    this.reason = reason;
    this.active = active;
    this.declaredAt = declaredAt;
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

  public Long getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(Long reviewerId) {
    this.reviewerId = reviewerId;
  }

  public Long getSubmissionId() {
    return submissionId;
  }

  public void setSubmissionId(Long submissionId) {
    this.submissionId = submissionId;
  }

  public COIType getType() {
    return type;
  }

  public void setType(COIType type) {
    this.type = type;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public LocalDateTime getDeclaredAt() {
    return declaredAt;
  }

  public void setDeclaredAt(LocalDateTime declaredAt) {
    this.declaredAt = declaredAt;
  }

  /** Enum định nghĩa các loại Conflict of Interest */
  public enum COIType {
    /** Reviewer là đồng tác giả */
    CO_AUTHOR,
    /** Reviewer là cộng tác viên */
    COLLABORATOR,
    /** Reviewer là cố vấn */
    ADVISOR,
    /** Cùng tổ chức */
    INSTITUTIONAL,
    /** Lý do khác */
    OTHER
  }

  public static class Builder {
    private Long id;
    private Long reviewerId;
    private Long submissionId;
    private COIType type;
    private String reason;
    private Boolean active = true;
    private LocalDateTime declaredAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder reviewerId(Long reviewerId) {
      this.reviewerId = reviewerId;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder type(COIType type) {
      this.type = type;
      return this;
    }

    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    public Builder active(Boolean active) {
      this.active = active;
      return this;
    }

    public Builder declaredAt(LocalDateTime declaredAt) {
      this.declaredAt = declaredAt;
      return this;
    }

    public ConflictOfInterest build() {
      return new ConflictOfInterest(id, reviewerId, submissionId, type, reason, active, declaredAt);
    }
  }
}
