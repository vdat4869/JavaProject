package com.uth.confms.storage.repository;

import com.uth.confms.storage.entity.BackupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, Long> {
    
    List<BackupRecord> findByConferenceIdOrderByCreatedAtDesc(Long conferenceId);
    
    List<BackupRecord> findByTypeOrderByCreatedAtDesc(BackupRecord.BackupType type);
    
    Optional<BackupRecord> findFirstByConferenceIdAndStatusOrderByCreatedAtDesc(
            Long conferenceId, BackupRecord.BackupStatus status);
    
    List<BackupRecord> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);
    
    List<BackupRecord> findByStatus(BackupRecord.BackupStatus status);
}
