package com.uth.confms.pc.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho PC member (thành viên Program Committee)
 *
 * <p>
 * PC member được mời bởi chair và có các trạng thái:
 *
 * <ul>
 * <li>PENDING - Đã được mời, chờ accept/decline
 * <li>ACCEPTED - Đã chấp nhận invitation
 * <li>DECLINED - Đã từ chối invitation
 * </ul>
 *
 * <p>
 * Chỉ PC members với status ACCEPTED mới có thể được assign reviews.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "pc_members")
@EntityListeners(AuditingEntityListener.class)
public class PCMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long conferenceId;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private PCMemberStatus status = PCMemberStatus.PENDING;

  @Column(columnDefinition = "TEXT")
  private String expertiseKeywords; // Từ khóa chuyên môn (comma-separated)

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "pc_member_expertise_topics", joinColumns = @JoinColumn(name = "pc_member_id"), inverseJoinColumns = @JoinColumn(name = "topic_id"))
  private java.util.List<com.uth.confms.conference.entity.Topic> expertiseTopics; // Các chủ đề chuyên môn

  @Column(nullable = true)
  private Integer preferredMaxAssignments; // Số lượng bái review tối đa mong muốn

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public PCMember() {
  }

  public PCMember(
      Long id,
      Long conferenceId,
      Long userId,
      PCMemberStatus status,
      String expertiseKeywords,
      List<com.uth.confms.conference.entity.Topic> expertiseTopics,
      Integer preferredMaxAssignments,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.userId = userId;
    this.status = status;
    this.expertiseKeywords = expertiseKeywords;
    this.expertiseTopics = expertiseTopics != null ? expertiseTopics : new ArrayList<>();
    this.preferredMaxAssignments = preferredMaxAssignments;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public PCMemberStatus getStatus() {
    return status;
  }

  public void setStatus(PCMemberStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getExpertiseKeywords() {
    return expertiseKeywords;
  }

  public void setExpertiseKeywords(String expertiseKeywords) {
    this.expertiseKeywords = expertiseKeywords;
  }

  public List<com.uth.confms.conference.entity.Topic> getExpertiseTopics() {
    return expertiseTopics != null ? expertiseTopics : new ArrayList<>();
  }

  public void setExpertiseTopics(List<com.uth.confms.conference.entity.Topic> expertiseTopics) {
    this.expertiseTopics = expertiseTopics != null ? expertiseTopics : new ArrayList<>();
  }

  public Integer getPreferredMaxAssignments() {
    return preferredMaxAssignments;
  }

  public void setPreferredMaxAssignments(Integer preferredMaxAssignments) {
    this.preferredMaxAssignments = preferredMaxAssignments;
  }

  /** Enum định nghĩa các trạng thái của PC member */
  public enum PCMemberStatus {
    /** Đã được mời, chờ accept/decline */
    PENDING,
    /** Đã chấp nhận invitation */
    ACCEPTED,
    /** Đã từ chối invitation */
    DECLINED
  }

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private Long userId;
    private PCMemberStatus status = PCMemberStatus.PENDING;
    private String expertiseKeywords;
    private List<com.uth.confms.conference.entity.Topic> expertiseTopics;
    private Integer preferredMaxAssignments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder status(PCMemberStatus status) {
      this.status = status;
      return this;
    }

    public Builder expertiseKeywords(String expertiseKeywords) {
      this.expertiseKeywords = expertiseKeywords;
      return this;
    }

    public Builder expertiseTopics(List<com.uth.confms.conference.entity.Topic> expertiseTopics) {
      this.expertiseTopics = expertiseTopics;
      return this;
    }

    public Builder preferredMaxAssignments(Integer preferredMaxAssignments) {
      this.preferredMaxAssignments = preferredMaxAssignments;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public PCMember build() {
      return new PCMember(
          id, conferenceId, userId, status, expertiseKeywords, expertiseTopics, preferredMaxAssignments, createdAt,
          updatedAt);
    }
  }
}
