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
  private Long paperId;

  @Column(nullable = false)
  private Long conferenceId;

  @Column(nullable = false)
  private Long trackId;

  @Column(nullable = false)
  private Long authorId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private CameraReadyStatus status = CameraReadyStatus.OPEN;

  @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CameraReadyVersion> versions = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "current_version_id")
  private CameraReadyVersion currentVersion;

  @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CameraReadyReview> reviews = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "metadata_id")
  private CameraReadyMetadata metadata;

  @Builder.Default
  private Boolean copyrightConfirmed = false;

  private Long copyrightConfirmedBy;

  private LocalDateTime copyrightConfirmedAt;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  // Business methods
  public boolean canUpload() {
    return status == CameraReadyStatus.OPEN || status == CameraReadyStatus.NEED_FIX;
  }

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
