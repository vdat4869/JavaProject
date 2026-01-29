package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
       List<AuditLog> findByUserId(Long userId);

       List<AuditLog> findByResourceAndResourceId(String resource, Long resourceId);

       List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

       List<AuditLog> findByAction(String action);

       Page<AuditLog> findByUserId(Long userId, Pageable pageable);

       Page<AuditLog> findByAction(String action, Pageable pageable);

       Page<AuditLog> findByResourceAndResourceId(String resource, Long resourceId, Pageable pageable);

       Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
