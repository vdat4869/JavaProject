package com.uth.confms.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho review template (mẫu đánh giá)
 *
 * <p>
 * Review template được sử dụng để tạo review nhanh chóng với các fields đã được
 * điền sẵn.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "review_templates")
@EntityListeners(AuditingEntityListener.class)
public class ReviewTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name; // Tên mẫu

  @Column(nullable = true)
  private Long conferenceId; // null = global, not null = riêng cho hội nghị

  @Column(columnDefinition = "TEXT")
  private String summary; // Tóm tắt mẫu

  @Column(columnDefinition = "TEXT")
  private String strengths; // Điểm mạnh mẫu

  @Column(columnDefinition = "TEXT")
  private String weaknesses; // Điểm yếu mẫu

  @Column(columnDefinition = "TEXT")
  private String comments; // Nhận xét mẫu

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Review.ReviewScore defaultScore; // Điểm mặc định

  @Column(nullable = false)
  private Boolean isDefault = false; // Mẫu mặc định?

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public ReviewTemplate() {
  }

  public ReviewTemplate(
      Long id,
      String name,
      Long conferenceId,
      String summary,
      String strengths,
      String weaknesses,
      String comments,
      Review.ReviewScore defaultScore,
      Boolean isDefault,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.conferenceId = conferenceId;
    this.summary = summary;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.comments = comments;
    this.defaultScore = defaultScore;
    this.isDefault = isDefault;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  // Getters and setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getStrengths() {
    return strengths;
  }

  public void setStrengths(String strengths) {
    this.strengths = strengths;
  }

  public String getWeaknesses() {
    return weaknesses;
  }

  public void setWeaknesses(String weaknesses) {
    this.weaknesses = weaknesses;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Review.ReviewScore getDefaultScore() {
    return defaultScore;
  }

  public void setDefaultScore(Review.ReviewScore defaultScore) {
    this.defaultScore = defaultScore;
  }

  public Boolean getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(Boolean isDefault) {
    this.isDefault = isDefault;
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

  public static class Builder {
    private Long id;
    private String name;
    private Long conferenceId;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String comments;
    private Review.ReviewScore defaultScore;
    private Boolean isDefault = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder summary(String summary) {
      this.summary = summary;
      return this;
    }

    public Builder strengths(String strengths) {
      this.strengths = strengths;
      return this;
    }

    public Builder weaknesses(String weaknesses) {
      this.weaknesses = weaknesses;
      return this;
    }

    public Builder comments(String comments) {
      this.comments = comments;
      return this;
    }

    public Builder defaultScore(Review.ReviewScore defaultScore) {
      this.defaultScore = defaultScore;
      return this;
    }

    public Builder isDefault(Boolean isDefault) {
      this.isDefault = isDefault;
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

    public ReviewTemplate build() {
      return new ReviewTemplate(
          id,
          name,
          conferenceId,
          summary,
          strengths,
          weaknesses,
          comments,
          defaultScore,
          isDefault,
          createdAt,
          updatedAt);
    }
  }
}
