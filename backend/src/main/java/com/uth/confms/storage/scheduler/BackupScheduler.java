package com.uth.confms.storage.scheduler;

import com.uth.confms.conference.repository.ConferenceRepository;
import com.uth.confms.storage.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled tasks cho automatic backup
 * 
 * <p>
 * Tasks:
 * <ul>
 * <li>Daily backup cho tất cả conferences
 * <li>Weekly full backup
 * <li>Cleanup old backups
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.backup.enabled", havingValue = "true", matchIfMissing = false)
public class BackupScheduler {

    private final BackupService backupService;
    private final ConferenceRepository conferenceRepository;

    @Value("${app.storage.backup.retention-days:30}")
    private int retentionDays;

    @Value("${app.storage.backup.daily.enabled:true}")
    private boolean dailyBackupEnabled;

    @Value("${app.storage.backup.weekly.enabled:true}")
    private boolean weeklyBackupEnabled;

    /**
     * Daily backup cho tất cả conferences
     * Chạy mỗi ngày lúc 2:00 AM
     */
    @Scheduled(cron = "${app.storage.backup.daily.cron:0 0 2 * * ?}")
    // Lên lịch sao lưu hàng ngày
    public void dailyBackup() {
        if (!dailyBackupEnabled) {
            log.debug("Daily backup is disabled");
            return;
        }

        log.info("Starting daily backup for all conferences");

        try {
            // Get all conferences
            List<Long> conferenceIds = conferenceRepository.findAll().stream()
                    .map(conference -> conference.getId())
                    .toList();

            int successCount = 0;
            int failureCount = 0;

            for (Long conferenceId : conferenceIds) {
                try {
                    backupService.backupConference(conferenceId);
                    successCount++;
                } catch (Exception e) {
                    log.error("Error backing up conference: {}", conferenceId, e);
                    failureCount++;
                }
            }

            log.info("Daily backup completed: {} successful, {} failed", successCount, failureCount);

        } catch (Exception e) {
            log.error("Error in daily backup scheduler", e);
        }
    }

    /**
     * Weekly full backup
     * Chạy mỗi Chủ Nhật lúc 3:00 AM
     */
    @Scheduled(cron = "${app.storage.backup.weekly.cron:0 0 3 ? * SUN}")
    // Lên lịch sao lưu hàng tuần
    public void weeklyFullBackup() {
        if (!weeklyBackupEnabled) {
            log.debug("Weekly backup is disabled");
            return;
        }

        log.info("Starting weekly full backup");

        try {
            backupService.backupAll();
            log.info("Weekly full backup completed");
        } catch (Exception e) {
            log.error("Error in weekly full backup", e);
        }
    }

    /**
     * Cleanup old backups
     * Chạy mỗi ngày lúc 4:00 AM
     */
    @Scheduled(cron = "${app.storage.backup.cleanup.cron:0 0 4 * * ?}")
    // Lên lịch dọn dẹp sao lưu cũ
    public void cleanupOldBackups() {
        log.info("Starting cleanup of old backups (retention: {} days)", retentionDays);

        try {
            int deletedCount = backupService.cleanupOldBackups(retentionDays);
            log.info("Cleanup completed: {} backups deleted", deletedCount);
        } catch (Exception e) {
            log.error("Error in backup cleanup", e);
        }
    }
}
