package com.uth.confms.storage.service.impl;

import com.uth.confms.storage.entity.BackupRecord;
import com.uth.confms.storage.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * NoOp implementation của BackupService cho MinIO/S3 storage
 * 
 * <p>
 * Với MinIO/S3, backup thường được handle bởi cloud provider:
 * <ul>
 * <li>Object versioning để giữ lại các phiên bản cũ
 * <li>Lifecycle policies để tự động archive/delete
 * <li>Cross-region replication cho disaster recovery
 * </ul>
 * 
 * <p>
 * Do đó, service này chỉ trả về empty results và log warnings.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.backend", havingValue = "minio", matchIfMissing = false)
public class NoOpBackupServiceImpl implements BackupService {

    @Override
    public BackupRecord backupConference(Long conferenceId) {
        log.info("Backup not available for MinIO storage. Use MinIO versioning/lifecycle policies instead.");
        return null;
    }

    @Override
    public BackupRecord backupAll() {
        log.info("Full backup not available for MinIO storage. Use MinIO versioning/lifecycle policies instead.");
        return null;
    }

    @Override
    public boolean restoreBackup(Long backupId) {
        log.warn("Restore not available for MinIO storage. Use MinIO console to restore from object versions.");
        return false;
    }

    @Override
    public boolean restoreConference(Long conferenceId) {
        log.warn("Restore not available for MinIO storage. Use MinIO console to restore from object versions.");
        return false;
    }

    @Override
    public List<BackupRecord> getBackupHistory(Long conferenceId) {
        return Collections.emptyList();
    }

    @Override
    public List<BackupRecord> getAllBackups() {
        return Collections.emptyList();
    }

    @Override
    public int cleanupOldBackups(int olderThanDays) {
        log.info("Cleanup not needed for MinIO storage. Use MinIO lifecycle policies.");
        return 0;
    }
}
