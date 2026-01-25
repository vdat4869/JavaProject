package com.uth.confms.storage.service;

import com.uth.confms.storage.entity.BackupRecord;

import java.util.List;

/**
 * Service để quản lý backup và recovery cho file storage
 */
public interface BackupService {
    
    /**
     * Tạo backup cho một conference cụ thể
     *
     * @param conferenceId ID của conference
     * @return BackupRecord chứa thông tin backup
     */
    BackupRecord backupConference(Long conferenceId);
    
    /**
     * Tạo full backup cho tất cả files
     *
     * @return BackupRecord chứa thông tin backup
     */
    BackupRecord backupAll();
    
    /**
     * Restore files từ backup
     *
     * @param backupId ID của backup record
     * @return true nếu restore thành công
     */
    boolean restoreBackup(Long backupId);
    
    /**
     * Restore files cho một conference từ backup gần nhất
     *
     * @param conferenceId ID của conference
     * @return true nếu restore thành công
     */
    boolean restoreConference(Long conferenceId);
    
    /**
     * Lấy danh sách backups cho một conference
     *
     * @param conferenceId ID của conference
     * @return List of BackupRecord
     */
    List<BackupRecord> getBackupHistory(Long conferenceId);
    
    /**
     * Lấy danh sách tất cả backups
     *
     * @return List of BackupRecord
     */
    List<BackupRecord> getAllBackups();
    
    /**
     * Xóa backup cũ (cleanup)
     *
     * @param olderThanDays Xóa backups cũ hơn số ngày này
     * @return Số lượng backups đã xóa
     */
    int cleanupOldBackups(int olderThanDays);
}
