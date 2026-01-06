package com.uth.confms.assignment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho assignment (phân công reviewer cho submission)
 *
 * <p>Một assignment liên kết một reviewer với một submission để review. Assignment có các trạng
 * thái:
 *
 * <ul>
 *   <li>ASSIGNED - Đã được phân công, chờ reviewer accept/decline
 *   <li>ACCEPTED - Reviewer đã chấp nhận assignment
 *   <li>DECLINED - Reviewer đã từ chối assignment
 *   <li>COMPLETED - Review đã hoàn thành
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "assignments")
@EntityListeners(AuditingEntityListener.class)
public class Assignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long submissionId;

  @Column(nullable = false)
  private Long reviewerId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private AssignmentStatus status = AssignmentStatus.ASSIGNED;

  @Column(nullable = false)
  private Boolean isPrimary = false;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime assignedAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  public Assignment() {
    // defaults handled in field declarations
  }

  public Assignment(
      Long id,
      Long submissionId,
      Long reviewerId,
      AssignmentStatus status,
      Boolean isPrimary,
      LocalDateTime assignedAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.submissionId = submissionId;
    this.reviewerId = reviewerId;
    this.status = status != null ? status : AssignmentStatus.ASSIGNED;
    this.isPrimary = isPrimary != null ? isPrimary : false;
    this.assignedAt = assignedAt;
    this.updatedAt = updatedAt;
  }

  // Getters and setters
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

  public Long getReviewerId() {
    return reviewerId;
  }

  public void setReviewerId(Long reviewerId) {
    this.reviewerId = reviewerId;
  }

  public AssignmentStatus getStatus() {
    return status;
  }

  public void setStatus(AssignmentStatus status) {
    this.status = status;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
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

  /** Enum định nghĩa các trạng thái của assignment */
  public enum AssignmentStatus {
    /** Đã được phân công, chờ reviewer accept/decline */
    ASSIGNED,
    /** Reviewer đã chấp nhận assignment */
    ACCEPTED,
    /** Reviewer đã từ chối assignment */
    DECLINED,
    /** Review đã hoàn thành */
    COMPLETED
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;
    private Boolean isPrimary = false;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submissionId(Long submissionId) {
      this.submissionId = submissionId;
      return this;
    }

    public Builder reviewerId(Long reviewerId) {
      this.reviewerId = reviewerId;
      return this;
    }

    public Builder status(AssignmentStatus status) {
      this.status = status != null ? status : AssignmentStatus.ASSIGNED;
      return this;
    }

    public Builder isPrimary(Boolean isPrimary) {
      this.isPrimary = isPrimary != null ? isPrimary : false;
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

    public Assignment build() {
      return new Assignment(id, submissionId, reviewerId, status, isPrimary, assignedAt, updatedAt);
    }
  }
}
