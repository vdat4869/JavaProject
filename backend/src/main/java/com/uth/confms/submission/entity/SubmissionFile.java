package com.uth.confms.submission.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity file submission
 */
@Entity
@Table(name = "submission_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubmissionFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @Column(nullable = false, length = 500)
    private String fileName; // Tên file gốc
    
    @Column(nullable = false, length = 500)
    private String storedPath; // Đường dẫn lưu trữ (MinIO hoặc local)
    
    @Column(nullable = false, length = 50)
    private String fileType; // MIME type
    
    @Column(nullable = false)
    private Long fileSize; // Kích thước file (bytes)
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileCategory category;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1; // Phiên bản file
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isLatest = true; // File mới nhất
    
    @Column(length = 100)
    private String checksum; // MD5 hoặc SHA256 để kiểm tra tính toàn vẹn
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    @Column(nullable = false)
    private Long uploadedBy; // User ID
    
    public enum FileCategory {
        MANUSCRIPT,      // Bản thảo chính
        SUPPLEMENTARY,   // Tài liệu bổ sung
        CAMERA_READY,    // Camera-ready version
        REVISION         // Bản chỉnh sửa
    }
}

