package com.uth.confms.email.scheduler;

import com.uth.confms.email.service.EmailQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks để process email queue
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueScheduler {

    private final EmailQueueService emailQueueService;

    /**
     * Process pending emails every minute
     */
    @Scheduled(fixedRate = 60000) // Every minute
    // Lên lịch xử lý hàng đợi email mỗi phút
    public void processEmailQueue() {
        try {
            int processed = emailQueueService.processPendingEmails();
            if (processed > 0) {
                log.info("Processed {} emails from queue", processed);
            }
        } catch (Exception e) {
            log.error("Error processing email queue", e);
        }
    }
}
