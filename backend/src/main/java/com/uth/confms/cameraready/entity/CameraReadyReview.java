package com.uth.confms.cameraready.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity đại diện cho đánh giá của Chair đối với bài nộp camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Entity
@Table(name = "camera_ready_reviews", indexes = {
        @Index(name = "idx_cr_review_submission_id", columnList = "submission_id"),
        @Index(name = "idx_cr_review_reviewed_at", columnList = "reviewed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraReadyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private CameraReadySubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private CameraReadyVersion version;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private ReviewDecision decision; // Quyết định duyệt (APPROVED/REJECTED)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú của reviewer

    @Column(name = "reviewed_by", nullable = false)
    private Long reviewedBy; // ID của người duyệt (Chair)

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt; // Thời gian duyệt

    @PrePersist
    protected void onCreate() {
        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
    }
}
