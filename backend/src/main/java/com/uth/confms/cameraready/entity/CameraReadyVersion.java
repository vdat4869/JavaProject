package com.uth.confms.cameraready.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity đại diện cho một phiên bản của bài nộp camera-ready.
 * Version là BẤT BIẾN - không thể sửa đổi hoặc xóa.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Entity
@Table(name = "camera_ready_versions", indexes = {
        @Index(name = "idx_cr_version_submission_id", columnList = "submission_id"),
        @Index(name = "idx_cr_version_uploaded_at", columnList = "uploaded_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cr_submission_version", columnNames = { "submission_id", "version_number" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraReadyVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private CameraReadySubmission submission;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber; // Số phiên bản (1, 2, 3...)

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename; // Tên file gốc

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath; // Đường dẫn lưu file trên ổ cứng/cloud

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256; // Checksum để kiểm tra toàn vẹn file

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "page_size", length = 20)
    private String pageSize;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_result", columnDefinition = "jsonb")
    private Map<String, Object> validationResult; // Kết quả validate PDF (JSON)

    @Column(name = "validation_passed")
    private Boolean validationPassed; // Đã qua bước kiểm tra kỹ thuật chưa

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt; // Thời gian tải lên

    public boolean isCurrent() {
        return submission != null
                && submission.getCurrentVersion() != null
                && submission.getCurrentVersion().getId().equals(this.id);
    }

    public boolean isValidationPassed() {
        return Boolean.TRUE.equals(validationPassed);
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
