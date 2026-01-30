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
    // Tạo bản sao lưu cho conference
    BackupRecord backupConference(Long conferenceId);

    /**
     * Tạo full backup cho tất cả files
     *
     * @return BackupRecord chứa thông tin backup
     */
    // Tạo bản sao lưu toàn bộ
    BackupRecord backupAll();

    /**
     * Restore files từ backup
     *
     * @param backupId ID của backup record
     * @return true nếu restore thành công
     */
    // Khôi phục từ bản sao lưu
    boolean restoreBackup(Long backupId);

    /**
     * Restore files cho một conference từ backup gần nhất
     *
     * @param conferenceId ID của conference
     * @return true nếu restore thành công
     */
    // Khôi phục dữ liệu conference
    boolean restoreConference(Long conferenceId);

    /**
     * Lấy danh sách backups cho một conference
     *
     * @param conferenceId ID của conference
     * @return List of BackupRecord
     */
    // Lấy lịch sử sao lưu
    List<BackupRecord> getBackupHistory(Long conferenceId);

    /**
     * Lấy danh sách tất cả backups
     *
     * @return List of BackupRecord
     */
    // Lấy danh sách tất cả bản sao lưu
    List<BackupRecord> getAllBackups();

    /**
     * Xóa backup cũ (cleanup)
     *
     * @param olderThanDays Xóa backups cũ hơn số ngày này
     * @return Số lượng backups đã xóa
     */
    // Dọn dẹp các bản sao lưu cũ
    int cleanupOldBackups(int olderThanDays);
}
