package com.uth.confms.email.service.impl;

import com.uth.confms.email.entity.EmailQueue;
import com.uth.confms.email.entity.EmailQuota;
import com.uth.confms.email.repository.EmailQueueRepository;
import com.uth.confms.email.service.EmailQueueService;
import com.uth.confms.email.service.EmailQuotaService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để quản lý email queue và retry failed emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueServiceImpl implements EmailQueueService {

    private final EmailQueueRepository queueRepository;
    private final EmailQuotaService quotaService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.email.from-name:UTH-ConfMS}")
    private String fromName;

    @Value("${app.email.retry.initial-delay-ms:1000}")
    private long initialDelayMs;

    @Value("${app.email.retry.multiplier:2.0}")
    private double retryMultiplier;

    @Override
    @Transactional
    // Xử lý các email trong hàng đợi
    public int processPendingEmails() {
        log.info("Processing pending emails from queue");

        List<EmailQueue> pendingEmails = queueRepository.findPendingEmailsReadyForRetry(
                EmailQueue.EmailStatus.PENDING, LocalDateTime.now());

        int successCount = 0;
        int failureCount = 0;

        for (EmailQueue emailQueue : pendingEmails) {
            try {
                emailQueue.setStatus(EmailQueue.EmailStatus.PROCESSING);
                queueRepository.save(emailQueue);

                if (retryEmail(emailQueue.getId())) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                log.error("Error processing queued email: {}", emailQueue.getId(), e);
                emailQueue.setStatus(EmailQueue.EmailStatus.PENDING);
                emailQueue.setErrorMessage(e.getMessage());
                queueRepository.save(emailQueue);
                failureCount++;
            }
        }

        log.info("Processed queued emails: {} successful, {} failed", successCount, failureCount);
        return successCount;
    }

    @Override
    @Transactional
    // Retry gửi lại email cụ thể
    public boolean retryEmail(Long queueId) {
        EmailQueue emailQueue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Email queue not found: " + queueId));

        if (!emailQueue.canRetry()) {
            log.warn("Email cannot be retried: {} (status: {}, retryCount: {})",
                    queueId, emailQueue.getStatus(), emailQueue.getRetryCount());
            return false;
        }

        // Check quota before retry
        if (!quotaService.isQuotaAvailable(EmailQuota.QuotaType.DAILY) ||
                !quotaService.isQuotaAvailable(EmailQuota.QuotaType.HOURLY)) {
            log.warn("Quota exceeded, deferring retry for email: {}", queueId);
            // Calculate next retry time with exponential backoff
            long delayMs = (long) (initialDelayMs * Math.pow(retryMultiplier, emailQueue.getRetryCount()));
            emailQueue.setNextRetryAt(LocalDateTime.now().plusSeconds(delayMs / 1000));
            emailQueue.setStatus(EmailQueue.EmailStatus.PENDING);
            queueRepository.save(emailQueue);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set from
            if (fromEmail != null && !fromEmail.isEmpty()) {
                try {
                    helper.setFrom(fromEmail, fromName != null ? fromName : "UTH-ConfMS");
                } catch (UnsupportedEncodingException e) {
                    helper.setFrom(fromEmail);
                }
            }

            // Set to, subject
            helper.setTo(emailQueue.getRecipient());
            helper.setSubject(emailQueue.getSubject());

            // Set content (HTML if template was used, plain text otherwise)
            boolean isHtml = emailQueue.getTemplateName() != null;
            helper.setText(emailQueue.getContent() != null ? emailQueue.getContent() : "", isHtml);

            // Send
            mailSender.send(message);

            // Record email sent
            quotaService.recordEmailSent(EmailQuota.QuotaType.DAILY);
            quotaService.recordEmailSent(EmailQuota.QuotaType.HOURLY);

            // Mark as sent
            emailQueue.setStatus(EmailQueue.EmailStatus.SENT);
            emailQueue.setSentAt(LocalDateTime.now());
            emailQueue.setErrorMessage(null);
            queueRepository.save(emailQueue);

            log.info("Queued email sent successfully: {} (retry count: {})",
                    emailQueue.getRecipient(), emailQueue.getRetryCount());
            return true;

        } catch (MessagingException e) {
            log.error("Error retrying email: {}", queueId, e);

            // Increment retry count
            emailQueue.incrementRetry();
            emailQueue.setErrorMessage(e.getMessage());

            // Calculate next retry time with exponential backoff
            if (emailQueue.canRetry()) {
                long delayMs = (long) (initialDelayMs * Math.pow(retryMultiplier, emailQueue.getRetryCount()));
                emailQueue.setNextRetryAt(LocalDateTime.now().plusSeconds(delayMs / 1000));
                emailQueue.setStatus(EmailQueue.EmailStatus.PENDING);
            }

            queueRepository.save(emailQueue);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    // Lấy danh sách emailpending
    public List<EmailQueue> getPendingEmails() {
        return queueRepository.findByStatusOrderByCreatedAtAsc(EmailQueue.EmailStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    // Lấy danh sách email đã failed
    public List<EmailQueue> getFailedEmails() {
        return queueRepository.findByStatusOrderByCreatedAtAsc(EmailQueue.EmailStatus.FAILED);
    }
}
