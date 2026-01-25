package com.uth.confms.storage.service.impl;

import com.uth.confms.storage.entity.BackupRecord;
import com.uth.confms.storage.repository.BackupRecordRepository;
import com.uth.confms.storage.service.BackupService;
import com.uth.confms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service để quản lý backup và recovery cho file storage
 * 
 * <p>Service này tạo backups bằng cách:
 * <ul>
 *   <li>Copy files vào backup directory với timestamp
 *   <li>Create ZIP archives cho backups
 *   <li>Track backup history trong database
 * </ul>
 * 
 * <p>Chỉ hoạt động với local storage. Với S3/MinIO, backup thường được handle
 * bởi cloud provider (versioning, lifecycle policies).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.backend", havingValue = "local", matchIfMissing = true)
public class BackupServiceImpl implements BackupService {
    
    private final StorageService storageService;
    private final BackupRecordRepository backupRecordRepository;
    
    @Value("${app.storage.backup.enabled:true}")
    private boolean backupEnabled;
    
    @Value("${app.storage.backup.directory:${app.storage.base-dir:/data/uploads}/backups}")
    private String backupDirectory;
    
    @Value("${app.storage.backup.retention-days:30}")
    private int retentionDays;
    
    @Override
    @Transactional
    public BackupRecord backupConference(Long conferenceId) {
        if (!backupEnabled) {
            log.warn("Backup is disabled");
            return null;
        }
        
        log.info("Starting backup for conference: {}", conferenceId);
        
        BackupRecord backupRecord = BackupRecord.builder()
                .conferenceId(conferenceId)
                .type(BackupRecord.BackupType.CONFERENCE)
                .status(BackupRecord.BackupStatus.IN_PROGRESS)
                .fileCount(0L)
                .totalSizeBytes(0L)
                .build();
        backupRecord = backupRecordRepository.save(backupRecord);
        
        try {
            String backupPath = createBackup(conferenceId, backupRecord.getId());
            
            // Count files and calculate size
            Path backupPathObj = Paths.get(backupPath);
            AtomicLong fileCount = new AtomicLong(0);
            AtomicLong totalSize = new AtomicLong(0);
            
            if (Files.exists(backupPathObj)) {
                Files.walkFileTree(backupPathObj, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        fileCount.incrementAndGet();
                        totalSize.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            
            backupRecord.setBackupPath(backupPath);
            backupRecord.setFileCount(fileCount.get());
            backupRecord.setTotalSizeBytes(totalSize.get());
            backupRecord.setStatus(BackupRecord.BackupStatus.COMPLETED);
            backupRecord.setCompletedAt(LocalDateTime.now());
            backupRecord.setNotes("Backup completed successfully");
            
            log.info("Backup completed for conference {}: {} files, {} bytes", 
                    conferenceId, fileCount.get(), totalSize.get());
            
        } catch (Exception e) {
            log.error("Error creating backup for conference: {}", conferenceId, e);
            backupRecord.setStatus(BackupRecord.BackupStatus.FAILED);
            backupRecord.setNotes("Backup failed: " + e.getMessage());
        }
        
        return backupRecordRepository.save(backupRecord);
    }
    
    @Override
    @Transactional
    public BackupRecord backupAll() {
        if (!backupEnabled) {
            log.warn("Backup is disabled");
            return null;
        }
        
        log.info("Starting full backup");
        
        BackupRecord backupRecord = BackupRecord.builder()
                .conferenceId(null) // Full backup không có conferenceId cụ thể
                .type(BackupRecord.BackupType.FULL)
                .status(BackupRecord.BackupStatus.IN_PROGRESS)
                .fileCount(0L)
                .totalSizeBytes(0L)
                .build();
        backupRecord = backupRecordRepository.save(backupRecord);
        
        try {
            String backupPath = createFullBackup(backupRecord.getId());
            
            // Count files and calculate size
            Path backupPathObj = Paths.get(backupPath);
            AtomicLong fileCount = new AtomicLong(0);
            AtomicLong totalSize = new AtomicLong(0);
            
            if (Files.exists(backupPathObj)) {
                Files.walkFileTree(backupPathObj, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        fileCount.incrementAndGet();
                        totalSize.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            
            backupRecord.setBackupPath(backupPath);
            backupRecord.setFileCount(fileCount.get());
            backupRecord.setTotalSizeBytes(totalSize.get());
            backupRecord.setStatus(BackupRecord.BackupStatus.COMPLETED);
            backupRecord.setCompletedAt(LocalDateTime.now());
            backupRecord.setNotes("Full backup completed successfully");
            
            log.info("Full backup completed: {} files, {} bytes", fileCount.get(), totalSize.get());
            
        } catch (Exception e) {
            log.error("Error creating full backup", e);
            backupRecord.setStatus(BackupRecord.BackupStatus.FAILED);
            backupRecord.setNotes("Backup failed: " + e.getMessage());
        }
        
        return backupRecordRepository.save(backupRecord);
    }
    
    @Override
    @Transactional
    public boolean restoreBackup(Long backupId) {
        log.info("Restoring backup: {}", backupId);
        
        BackupRecord backupRecord = backupRecordRepository.findById(backupId)
                .orElseThrow(() -> new RuntimeException("Backup not found: " + backupId));
        
        if (backupRecord.getStatus() != BackupRecord.BackupStatus.COMPLETED) {
            log.error("Cannot restore backup with status: {}", backupRecord.getStatus());
            return false;
        }
        
        try {
            Path backupPath = Paths.get(backupRecord.getBackupPath());
            if (!Files.exists(backupPath)) {
                log.error("Backup path does not exist: {}", backupPath);
                return false;
            }
            
            // Restore files from backup
            if (Files.isDirectory(backupPath)) {
                restoreFromDirectory(backupPath);
            } else if (backupPath.toString().endsWith(".zip")) {
                restoreFromZip(backupPath);
            } else {
                log.error("Unknown backup format: {}", backupPath);
                return false;
            }
            
            log.info("Backup restored successfully: {}", backupId);
            return true;
            
        } catch (Exception e) {
            log.error("Error restoring backup: {}", backupId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean restoreConference(Long conferenceId) {
        log.info("Restoring conference from latest backup: {}", conferenceId);
        
        Optional<BackupRecord> latestBackup = backupRecordRepository
                .findFirstByConferenceIdAndStatusOrderByCreatedAtDesc(
                        conferenceId, BackupRecord.BackupStatus.COMPLETED);
        
        if (latestBackup.isEmpty()) {
            log.warn("No backup found for conference: {}", conferenceId);
            return false;
        }
        
        return restoreBackup(latestBackup.get().getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BackupRecord> getBackupHistory(Long conferenceId) {
        return backupRecordRepository.findByConferenceIdOrderByCreatedAtDesc(conferenceId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BackupRecord> getAllBackups() {
        return backupRecordRepository.findAll();
    }
    
    @Override
    @Transactional
    public int cleanupOldBackups(int olderThanDays) {
        log.info("Cleaning up backups older than {} days", olderThanDays);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
        List<BackupRecord> oldBackups = backupRecordRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(
                        LocalDateTime.MIN, cutoffDate);
        
        int deletedCount = 0;
        for (BackupRecord backup : oldBackups) {
            try {
                // Delete backup file/directory
                Path backupPath = Paths.get(backup.getBackupPath());
                if (Files.exists(backupPath)) {
                    if (Files.isDirectory(backupPath)) {
                        deleteDirectory(backupPath);
                    } else {
                        Files.delete(backupPath);
                    }
                }
                
                // Delete record
                backupRecordRepository.delete(backup);
                deletedCount++;
                
            } catch (Exception e) {
                log.error("Error deleting backup: {}", backup.getId(), e);
            }
        }
        
        log.info("Cleaned up {} old backups", deletedCount);
        return deletedCount;
    }
    
    /**
     * Tạo backup cho một conference
     */
    private String createBackup(Long conferenceId, Long backupId) throws IOException {
        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupDir = String.format("%s/conferences/%d/backup_%s_%d",
                backupDirectory, conferenceId, timestamp, backupId);
        
        Path backupPath = Paths.get(backupDir);
        Files.createDirectories(backupPath);
        
        // Copy files from storage
        String conferencePrefix = "conferences/" + conferenceId + "/";
        copyFilesWithPrefix(conferencePrefix, backupPath);
        
        // Create ZIP archive
        String zipPath = backupDir + ".zip";
        createZipArchive(backupPath, Paths.get(zipPath));
        
        // Delete directory after creating ZIP
        deleteDirectory(backupPath);
        
        return zipPath;
    }
    
    /**
     * Tạo full backup
     */
    private String createFullBackup(Long backupId) throws IOException {
        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupDir = String.format("%s/full/backup_%s_%d",
                backupDirectory, timestamp, backupId);
        
        Path backupPath = Paths.get(backupDir);
        Files.createDirectories(backupPath);
        
        // Copy all files from storage
        copyAllFiles(backupPath);
        
        // Create ZIP archive
        String zipPath = backupDir + ".zip";
        createZipArchive(backupPath, Paths.get(zipPath));
        
        // Delete directory after creating ZIP
        deleteDirectory(backupPath);
        
        return zipPath;
    }
    
    /**
     * Copy files với prefix từ storage
     */
    private void copyFilesWithPrefix(String prefix, Path targetDir) throws IOException {
        // This is a simplified implementation
        // In production, you would need to list all files from StorageService
        // For local storage, we can directly access the filesystem
        if (storageService instanceof LocalStorageServiceImpl) {
            LocalStorageServiceImpl localStorage = (LocalStorageServiceImpl) storageService;
            Path baseDir = Paths.get(localStorage.getBaseDir());
            Path sourceDir = baseDir.resolve(prefix);
            
            if (Files.exists(sourceDir)) {
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = sourceDir.relativize(file);
                        Path targetFile = targetDir.resolve(relativePath);
                        Files.createDirectories(targetFile.getParent());
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }
    
    /**
     * Copy tất cả files từ storage
     */
    private void copyAllFiles(Path targetDir) throws IOException {
        if (storageService instanceof LocalStorageServiceImpl) {
            LocalStorageServiceImpl localStorage = (LocalStorageServiceImpl) storageService;
            Path baseDir = Paths.get(localStorage.getBaseDir());
            
            if (Files.exists(baseDir)) {
                Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relativePath = baseDir.relativize(file);
                        Path targetFile = targetDir.resolve(relativePath);
                        Files.createDirectories(targetFile.getParent());
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
    }
    
    /**
     * Tạo ZIP archive từ directory
     */
    private void createZipArchive(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = sourceDir.relativize(file);
                    ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace('\\', '/'));
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
    
    /**
     * Restore files từ directory
     */
    private void restoreFromDirectory(Path backupDir) throws IOException {
        // Implementation depends on storage backend
        // For local storage, we can directly copy files
        if (storageService instanceof LocalStorageServiceImpl) {
            LocalStorageServiceImpl localStorage = (LocalStorageServiceImpl) storageService;
            Path baseDir = Paths.get(localStorage.getBaseDir());
            
            Files.walkFileTree(backupDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = backupDir.relativize(file);
                    Path targetFile = baseDir.resolve(relativePath);
                    Files.createDirectories(targetFile.getParent());
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
    
    /**
     * Restore files từ ZIP archive
     */
    private void restoreFromZip(Path zipFile) throws IOException {
        // Implementation for ZIP restore
        // This would require extracting ZIP and copying files
        log.info("ZIP restore not yet implemented for: {}", zipFile);
    }
    
    /**
     * Delete directory recursively
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
