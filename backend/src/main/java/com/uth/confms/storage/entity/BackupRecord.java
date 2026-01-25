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
    private String backupPath; // Path to backup file/directory
    
    @Column(nullable = false)
    private Long fileCount; // Number of files backed up
    
    @Column(nullable = false)
    private Long totalSizeBytes; // Total size of backup in bytes
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BackupStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String notes; // Additional notes or error messages
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    public enum BackupType {
        FULL,           // Full backup of all files
        INCREMENTAL,    // Incremental backup (only new/changed files)
        CONFERENCE     // Backup for specific conference
    }
    
    public enum BackupStatus {
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
}
