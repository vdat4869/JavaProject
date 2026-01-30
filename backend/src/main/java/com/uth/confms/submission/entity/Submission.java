package com.uth.confms.submission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho submission (bài nộp) của author
 *
 * <p>
 * Submission có các trạng thái:
 *
 * <ul>
 * <li>DRAFT - Đang soạn thảo, chưa submit
 * <li>SUBMITTED - Đã submit, chờ review
 * <li>UNDER_REVIEW - Đang được review
 * <li>ACCEPTED - Đã được chấp nhận
 * <li>REJECTED - Đã bị từ chối
 * <li>CAMERA_READY - Đã upload camera-ready version
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "submissions")
@EntityListeners(AuditingEntityListener.class)
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long conferenceId;

  @Column(nullable = false)
  private Long authorId; // Người nộp bài

  @Column(nullable = false)
  private String title; // Tiêu đề

  @Column(columnDefinition = "TEXT")
  private String abstractText; // Tóm tắt

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private SubmissionStatus status = SubmissionStatus.DRAFT; // Trạng thái

  private String pdfFilePath; // Đường dẫn file PDF

  private Long trackId; // Track (chủ đề)

  @Column(columnDefinition = "TEXT")
  private String keywords; // Từ khóa

  @Column(nullable = false)
  private Boolean withdrawn = false; // Đã rút bài?

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public Submission() {
    this.status = SubmissionStatus.DRAFT;
    this.withdrawn = false;
  }

  public Submission(
      Long id,
      Long conferenceId,
      Long authorId,
      String title,
      String abstractText,
      SubmissionStatus status,
      String pdfFilePath,
      Long trackId,
      String keywords,
      Boolean withdrawn,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.authorId = authorId;
    this.title = title;
    this.abstractText = abstractText;
    this.status = status != null ? status : SubmissionStatus.DRAFT;
    this.pdfFilePath = pdfFilePath;
    this.trackId = trackId;
    this.keywords = keywords;
    this.withdrawn = withdrawn != null ? withdrawn : false;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAbstractText() {
    return abstractText;
  }

  public void setAbstractText(String abstractText) {
    this.abstractText = abstractText;
  }

  public SubmissionStatus getStatus() {
    return status;
  }

  public void setStatus(SubmissionStatus status) {
    this.status = status;
  }

  public String getPdfFilePath() {
    return pdfFilePath;
  }

  public void setPdfFilePath(String pdfFilePath) {
    this.pdfFilePath = pdfFilePath;
  }

  public Long getTrackId() {
    return trackId;
  }

  public void setTrackId(Long trackId) {
    this.trackId = trackId;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public Boolean getWithdrawn() {
    return withdrawn;
  }

  public void setWithdrawn(Boolean withdrawn) {
    this.withdrawn = withdrawn;
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

  /** Enum định nghĩa các trạng thái của submission */
  public enum SubmissionStatus {
    /** Đang soạn thảo, chưa submit */
    DRAFT,
    /** Đã submit, chờ review */
    SUBMITTED,
    /** Đang được review */
    UNDER_REVIEW,
    /** Đã hoàn thành phản biện (đủ số lượng review) */
    REVIEWED,
    /** Đã được chấp nhận */
    ACCEPTED,
    /** Đã bị từ chối */
    REJECTED,
    /** Đã upload camera-ready version */
    CAMERA_READY
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private Long authorId;
    private String title;
    private String abstractText;
    private SubmissionStatus status = SubmissionStatus.DRAFT;
    private String pdfFilePath;
    private Long trackId;
    private String keywords;
    private Boolean withdrawn = false;
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

    public Builder authorId(Long authorId) {
      this.authorId = authorId;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder abstractText(String abstractText) {
      this.abstractText = abstractText;
      return this;
    }

    public Builder status(SubmissionStatus status) {
      this.status = status != null ? status : SubmissionStatus.DRAFT;
      return this;
    }

    public Builder pdfFilePath(String pdfFilePath) {
      this.pdfFilePath = pdfFilePath;
      return this;
    }

    public Builder trackId(Long trackId) {
      this.trackId = trackId;
      return this;
    }

    public Builder keywords(String keywords) {
      this.keywords = keywords;
      return this;
    }

    public Builder withdrawn(Boolean withdrawn) {
      this.withdrawn = withdrawn != null ? withdrawn : false;
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

    public Submission build() {
      return new Submission(
          id,
          conferenceId,
          authorId,
          title,
          abstractText,
          status,
          pdfFilePath,
          trackId,
          keywords,
          withdrawn,
          createdAt,
          updatedAt);
    }
  }
}
