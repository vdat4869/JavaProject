package com.uth.confms.cameraready.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity đại diện cho bài nộp camera-ready.
 * Mỗi bài báo đã được chấp nhận sẽ có một CameraReadySubmission tương ứng.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Entity
@Table(name = "camera_ready_submissions", indexes = {
        @Index(name = "idx_cr_submission_paper_id", columnList = "paper_id"),
        @Index(name = "idx_cr_submission_conference_id", columnList = "conference_id"),
        @Index(name = "idx_cr_submission_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraReadySubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Mã bài báo (liên kết với module submission).
     */
    @Column(name = "paper_id", nullable = false, unique = true)
    private UUID paperId;

    /**
     * Mã hội nghị.
     */
    @Column(name = "conference_id", nullable = false)
    private UUID conferenceId;

    /**
     * Mã track (nếu có).
     */
    @Column(name = "track_id")
    private UUID trackId;

    /**
     * Trạng thái hiện tại của bài nộp.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CameraReadyStatus status = CameraReadyStatus.NOT_OPEN;

    /**
     * Đã xác nhận bản quyền chưa.
     */
    @Column(name = "copyright_confirmed")
    @Builder.Default
    private Boolean copyrightConfirmed = false;

    /**
     * Thời gian xác nhận bản quyền.
     */
    @Column(name = "copyright_confirmed_at")
    private LocalDateTime copyrightConfirmedAt;

    /**
     * Người xác nhận bản quyền (user_id).
     */
    @Column(name = "copyright_confirmed_by")
    private UUID copyrightConfirmedBy;

    /**
     * Phiên bản hiện tại.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private CameraReadyVersion currentVersion;

    /**
     * Danh sách tất cả các phiên bản đã tải lên.
     */
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("versionNumber DESC")
    @Builder.Default
    private List<CameraReadyVersion> versions = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Business Methods ====================

    public boolean canUpload() {
        return status.canUpload();
    }

    public boolean canReview() {
        return status.canReview();
    }

    public boolean canTransitionTo(CameraReadyStatus newStatus) {
        return status.canTransitionTo(newStatus);
    }

    public void transitionTo(CameraReadyStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển từ %s sang %s", status, newStatus));
        }
        this.status = newStatus;
    }

    public void addVersion(CameraReadyVersion version) {
        versions.add(version);
        version.setSubmission(this);
        this.currentVersion = version;
        
        if (status == CameraReadyStatus.OPEN || status == CameraReadyStatus.NEED_FIX) {
            this.status = CameraReadyStatus.SUBMITTED;
        }
    }

    public void confirmCopyright(UUID userId) {
        if (Boolean.TRUE.equals(this.copyrightConfirmed)) {
            throw new IllegalStateException("Bản quyền đã được xác nhận trước đó");
        }
        this.copyrightConfirmed = true;
        this.copyrightConfirmedAt = LocalDateTime.now();
        this.copyrightConfirmedBy = userId;
    }

    public int getNextVersionNumber() {
        return versions.stream()
                .mapToInt(CameraReadyVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;
    }
}
