package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
  List<AuditLog> findByUserId(Long userId);

  List<AuditLog> findByResourceAndResourceId(String resource, Long resourceId);

  List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
