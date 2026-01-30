package com.uth.confms.email.service;

import com.uth.confms.email.entity.EmailQueue;

import java.util.List;

/**
 * Service để quản lý email queue và retry failed emails
 */
public interface EmailQueueService {

    /**
     * Process pending emails in queue
     *
     * @return Number of emails processed successfully
     */
    // Xử lý các email trong hàng đợi
    int processPendingEmails();

    /**
     * Retry a specific queued email
     *
     * @param queueId Email queue ID
     * @return true if retry successful
     */
    // Thử gửi lại email cụ thể
    boolean retryEmail(Long queueId);

    /**
     * Get pending emails
     *
     * @return List of pending emails
     */
    // Lấy danh sách email đang chờ xử lý
    List<EmailQueue> getPendingEmails();

    /**
     * Get failed emails
     *
     * @return List of failed emails
     */
    // Lấy danh sách email đã gửi thất bại
    List<EmailQueue> getFailedEmails();
}
