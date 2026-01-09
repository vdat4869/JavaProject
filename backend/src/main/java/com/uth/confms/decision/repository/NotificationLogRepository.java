package com.uth.confms.decision.repository;

import com.uth.confms.decision.entity.NotificationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
  List<NotificationLog> findBySubmissionId(Long submissionId);

  List<NotificationLog> findByUserId(Long userId);

  List<NotificationLog> findByType(NotificationLog.NotificationType type);

  List<NotificationLog> findByStatus(NotificationLog.NotificationStatus status);
}
