package com.uth.confms.review.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho review (đánh giá bài nộp)
 *
 * <p>
 * Review được tạo bởi reviewer sau khi accept assignment. Review có các trạng
 * thái:
 *
 * <ul>
 * <li>DRAFT - Đang soạn thảo, chưa submit
 * <li>SUBMITTED - Đã submit, không thể chỉnh sửa
 * </ul>
 *
 * <p>
 * Review có các scores từ STRONG_ACCEPT đến STRONG_REJECT. Review có thể là
 * confidential (chỉ
 * chair/PC thấy) hoặc public (author thấy).
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long assignmentId; // ID phân công

  @Column(nullable = false)
  private Long submissionId; // ID bài báo

  @Column(nullable = false)
  private Long reviewerId; // ID người review

  @Column(columnDefinition = "TEXT")
  private String summary; // Tóm tắt bài báo

  @Column(columnDefinition = "TEXT")
  private String strengths; // Điểm mạnh

  @Column(columnDefinition = "TEXT")
  private String weaknesses; // Điểm yếu

  @Column(columnDefinition = "TEXT")
  private String comments; // Nhận xét chi tiết

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ReviewScore score; // Điểm đánh giá (Strong Accept -> Strong Reject)

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ReviewStatus status = ReviewStatus.DRAFT; // Trạng thái (DRAFT/SUBMITTED)

  @Column(nullable = false)
  private Boolean isConfidential = false; // Review bí mật (chỉ Chair thấy)

  @Column(nullable = true)
  private Integer overallRating; // Đánh giá chung (1-5)

  @Column(nullable = true)
  private Integer confidence; // Độ tự tin của reviewer (1-5)

  @Column(nullable = true)
  private Integer numericScore; // Điểm số dạng số (1-7)

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime submittedAt; // Thời gian submit

  public Review() {
  }

  public Review(
      Long id,
      Long assignmentId,
      Long submissionId,
      Long reviewerId,
      String summary,
      String strengths,
      String weaknesses,
      String comments,
      ReviewScore score,
      ReviewStatus status,
      Boolean isConfidential,
      Integer overallRating,
      Integer confidence,
      Integer numericScore,
      LocalDateTime createdAt,
      LocalDateTime submittedAt) {
    this.id = id;
    this.assignmentId = assignmentId;
    this.submissionId = submissionId;
    this.reviewerId = reviewerId;
    this.summary = summary;
    this.strengths = strengths;
    this.weaknesses = weaknesses;
    this.comments = comments;
    this.score = score;
    this.status = status;
    this.isConfidential = isConfidential;
    this.overallRating = overallRating;
    this.confidence = confidence;
    this.numericScore = numericScore;
    this.createdAt = createdAt;
    this.submittedAt = submittedAt;
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

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
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

  public ReviewScore getScore() {
    return score;
  }

  public void setScore(ReviewScore score) {
    this.score = score;
  }

  public ReviewStatus getStatus() {
    return status;
  }

  public void setStatus(ReviewStatus status) {
    this.status = status;
  }

  public Boolean getIsConfidential() {
    return isConfidential;
  }

  public void setIsConfidential(Boolean isConfidential) {
    this.isConfidential = isConfidential;
  }

  public Integer getOverallRating() {
    return overallRating;
  }

  public void setOverallRating(Integer overallRating) {
    this.overallRating = overallRating;
  }

  public Integer getConfidence() {
    return confidence;
  }

  public void setConfidence(Integer confidence) {
    this.confidence = confidence;
  }

  public Integer getNumericScore() {
    return numericScore;
  }

  public void setNumericScore(Integer numericScore) {
    this.numericScore = numericScore;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  /** Enum định nghĩa các điểm đánh giá (score) của review */
  public enum ReviewScore {
    /** Chấp nhận mạnh mẽ */
    STRONG_ACCEPT,
    /** Chấp nhận */
    ACCEPT,
    /** Chấp nhận yếu */
    WEAK_ACCEPT,
    /** Ranh giới (có thể chấp nhận hoặc từ chối) */
    BORDERLINE,
    /** Từ chối yếu */
    WEAK_REJECT,
    /** Từ chối */
    REJECT,
    /** Từ chối mạnh mẽ */
    STRONG_REJECT;

    /**
     * Map ReviewScore enum sang numeric score (1-7)
     *
     * @return Numeric score: STRONG_ACCEPT=7, STRONG_REJECT=1
     */
    public int toNumericScore() {
      return switch (this) {
        case STRONG_ACCEPT -> 7;
        case ACCEPT -> 6;
        case WEAK_ACCEPT -> 5;
        case BORDERLINE -> 4;
        case WEAK_REJECT -> 3;
        case REJECT -> 2;
        case STRONG_REJECT -> 1;
      };
    }
  }

  /** Enum định nghĩa các trạng thái của review */
  public enum ReviewStatus {
    /** Đang soạn thảo, chưa submit */
    DRAFT,
    /** Đã submit, không thể chỉnh sửa */
    SUBMITTED
  }

  public static class Builder {
    private Long id;
    private Long assignmentId;
    private Long submissionId;
    private Long reviewerId;
    private String summary;
    private String strengths;
    private String weaknesses;
    private String comments;
    private ReviewScore score;
    private ReviewStatus status = ReviewStatus.DRAFT;
    private Boolean isConfidential = false;
    private Integer overallRating;
    private Integer confidence;
    private Integer numericScore;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder assignmentId(Long assignmentId) {
      this.assignmentId = assignmentId;
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

    public Builder score(ReviewScore score) {
      this.score = score;
      return this;
    }

    public Builder status(ReviewStatus status) {
      this.status = status;
      return this;
    }

    public Builder isConfidential(Boolean isConfidential) {
      this.isConfidential = isConfidential;
      return this;
    }

    public Builder overallRating(Integer overallRating) {
      this.overallRating = overallRating;
      return this;
    }

    public Builder confidence(Integer confidence) {
      this.confidence = confidence;
      return this;
    }

    public Builder numericScore(Integer numericScore) {
      this.numericScore = numericScore;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder submittedAt(LocalDateTime submittedAt) {
      this.submittedAt = submittedAt;
      return this;
    }

    public Review build() {
      return new Review(
          id,
          assignmentId,
          submissionId,
          reviewerId,
          summary,
          strengths,
          weaknesses,
          comments,
          score,
          status,
          isConfidential,
          overallRating,
          confidence,
          numericScore,
          createdAt,
          submittedAt);
    }
  }
}
