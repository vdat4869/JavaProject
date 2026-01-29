package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
       List<AuditLog> findByUserId(Long userId);

       List<AuditLog> findByResourceAndResourceId(String resource, Long resourceId);

       List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

       List<AuditLog> findByAction(String action);

       Page<AuditLog> findByUserId(Long userId, Pageable pageable);

       Page<AuditLog> findByAction(String action, Pageable pageable);

       Page<AuditLog> findByResourceAndResourceId(String resource, Long resourceId, Pageable pageable);

       Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

       @Query("SELECT a FROM AuditLog a WHERE " +
                     "(a.userId = :userId OR :userId IS NULL) AND " +
                     "(a.action = :action OR :action IS NULL) AND " +
                     "(a.resource = :resource OR :resource IS NULL) AND " +
                     "(a.resourceId = :resourceId OR :resourceId IS NULL) AND " +
                     "(a.timestamp >= :startDate OR :startDate IS NULL) AND " +
                     "(a.timestamp <= :endDate OR :endDate IS NULL)")
       Page<AuditLog> findWithFilters(
                     @Param("userId") Long userId,
                     @Param("action") String action,
                     @Param("resource") String resource,
                     @Param("resourceId") Long resourceId,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate,
                     Pageable pageable);
}
