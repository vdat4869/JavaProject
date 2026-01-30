package com.uth.confms.storage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity để track backup history
 */
@Entity
@Table(name = "backup_records")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conferenceId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BackupType type;

    @Column(nullable = false)
    private String backupPath; // Path đến file/directory backup

    @Column(nullable = false)
    private Long fileCount; // Số lượng files đã backup

    @Column(nullable = false)
    private Long totalSizeBytes; // Tổng kích thước backup (bytes)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BackupStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes; // Ghi chú hoặc thông báo lỗi

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public enum BackupType {
        FULL, // Sao lưu toàn bộ
        INCREMENTAL, // Sao lưu tăng dần (chỉ files mới/thay đổi)
        CONFERENCE // Sao lưu cho conference cụ thể
    }

    public enum BackupStatus {
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
