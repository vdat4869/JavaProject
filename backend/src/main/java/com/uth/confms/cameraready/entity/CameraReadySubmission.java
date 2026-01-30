package com.uth.confms.cameraready.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "camera_ready_submissions")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CameraReadySubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private Long paperId; // ID của bài báo gốc (Submission)

  @Column(nullable = false)
  private Long conferenceId;

  @Column(nullable = false)
  private Long trackId;

  @Column(nullable = false)
  private Long authorId; // ID của tác giả chính

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private CameraReadyStatus status = CameraReadyStatus.OPEN; // Trạng thái hiện tại

  @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CameraReadyVersion> versions = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "current_version_id")
  private CameraReadyVersion currentVersion; // Phiên bản được chọn làm chính thức

  @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CameraReadyReview> reviews = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "metadata_id")
  private CameraReadyMetadata metadata; // Metadata bổ sung (DOI, trang, v.v.)

  @Builder.Default
  private Boolean copyrightConfirmed = false; // Đã xác nhận bản quyền chưa

  private Long copyrightConfirmedBy;

  private LocalDateTime copyrightConfirmedAt;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  // Business methods
  /**
   * Kiểm tra xem tác giả có thể upload phiên bản mới không.
   */
  public boolean canUpload() {
    return status == CameraReadyStatus.OPEN || status == CameraReadyStatus.NEED_FIX;
  }

  /**
   * Kiểm tra xem Chair có thể review submission này không.
   */
  public boolean canReview() {
    return status == CameraReadyStatus.SUBMITTED;
  }

  public void addVersion(CameraReadyVersion version) {
    versions.add(version);
    version.setSubmission(this);
    this.currentVersion = version;
  }

  public void setCurrentVersion(CameraReadyVersion version) {
    this.currentVersion = version;
  }

  public CameraReadyVersion getCurrentVersion() {
    return currentVersion;
  }

  public int getNextVersionNumber() {
    return versions.size() + 1;
  }

  public void confirmCopyright(Long userId) {
    this.copyrightConfirmed = true;
    this.copyrightConfirmedBy = userId;
    this.copyrightConfirmedAt = LocalDateTime.now();
  }

  public void transitionTo(CameraReadyStatus newStatus) {
    this.status = newStatus;
  }

  public CameraReadyStatus getStatus() {
    return status;
  }

  public void setStatus(CameraReadyStatus status) {
    this.status = status;
  }

  public Long getTrackId() {
    return trackId;
  }
}
