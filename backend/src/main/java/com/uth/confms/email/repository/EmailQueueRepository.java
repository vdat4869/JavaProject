package com.uth.confms.email.repository;

import com.uth.confms.email.entity.EmailQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {

    List<EmailQueue> findByStatusOrderByCreatedAtAsc(EmailQueue.EmailStatus status);

    @Query("SELECT e FROM EmailQueue e WHERE e.status = :status AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now) ORDER BY e.createdAt ASC")
    // Tìm các email đang chờ xử lý và sẵn sàng retry
    List<EmailQueue> findPendingEmailsReadyForRetry(EmailQueue.EmailStatus status, LocalDateTime now);

    List<EmailQueue> findByRecipientOrderByCreatedAtDesc(String recipient);

    List<EmailQueue> findByStatusAndRetryCountLessThan(EmailQueue.EmailStatus status, Integer maxRetries);
}
