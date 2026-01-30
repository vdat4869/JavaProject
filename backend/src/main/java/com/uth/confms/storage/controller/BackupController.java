package com.uth.confms.storage.controller;

import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.storage.entity.BackupRecord;
import com.uth.confms.storage.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller để quản lý backups
 * 
 * <p>
 * Endpoints:
 * <ul>
 * <li>POST /api/storage/backup/conference/{conferenceId} - Create backup cho
 * conference
 * <li>POST /api/storage/backup/all - Create full backup
 * <li>POST /api/storage/backup/{backupId}/restore - Restore từ backup
 * <li>POST /api/storage/backup/conference/{conferenceId}/restore - Restore
 * conference từ latest backup
 * <li>GET /api/storage/backup/conference/{conferenceId} - Get backup history
 * cho conference
 * <li>GET /api/storage/backup - Get all backups
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/storage/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    @PostMapping("/conference/{conferenceId}")
    public ResponseEntity<ApiResponse<BackupRecord>> backupConference(
            @PathVariable Long conferenceId) {
        log.info("Manual backup requested for conference: {}", conferenceId);
        // API tạo bản sao lưu conference
        BackupRecord backup = backupService.backupConference(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(backup));
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<BackupRecord>> backupAll() {
        log.info("Manual full backup requested");
        // API tạo bản sao lưu toàn bộ
        BackupRecord backup = backupService.backupAll();
        return ResponseEntity.ok(ApiResponse.success(backup));
    }

    @PostMapping("/{backupId}/restore")
    public ResponseEntity<ApiResponse<Boolean>> restoreBackup(@PathVariable Long backupId) {
        log.info("Restore requested for backup: {}", backupId);
        // API khôi phục từ bản sao lưu
        boolean success = backupService.restoreBackup(backupId);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    @PostMapping("/conference/{conferenceId}/restore")
    public ResponseEntity<ApiResponse<Boolean>> restoreConference(@PathVariable Long conferenceId) {
        log.info("Restore requested for conference: {}", conferenceId);
        // API khôi phục conference
        boolean success = backupService.restoreConference(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(success));
    }

    @GetMapping("/conference/{conferenceId}")
    public ResponseEntity<ApiResponse<List<BackupRecord>>> getBackupHistory(
            @PathVariable Long conferenceId) {
        // API lấy lịch sử sao lưu
        List<BackupRecord> backups = backupService.getBackupHistory(conferenceId);
        return ResponseEntity.ok(ApiResponse.success(backups));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BackupRecord>>> getAllBackups() {
        // API lấy danh sách tất cả bản sao lưu
        List<BackupRecord> backups = backupService.getAllBackups();
        return ResponseEntity.ok(ApiResponse.success(backups));
    }
}
