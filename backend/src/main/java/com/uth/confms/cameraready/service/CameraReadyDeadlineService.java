package com.uth.confms.cameraready.service;

/**
 * Service để quản lý deadline cho camera-ready phase.
 * 
 * @author UTH-ConfMS Team
 * @version 1.0.0
 */
public interface CameraReadyDeadlineService {

    /**
     * Check deadline và auto-close camera-ready phase nếu deadline đã hết
     * 
     * @param conferenceId Conference ID
     */
    void checkAndCloseDeadline(Long conferenceId);

    /**
     * Send reminder notifications trước deadline
     * 
     * @param conferenceId       Conference ID
     * @param daysBeforeDeadline Số ngày trước deadline để gửi reminder
     */
    void sendDeadlineReminders(Long conferenceId, int daysBeforeDeadline);

    /**
     * Check tất cả conferences có deadline đã hết
     */
    void checkAllDeadlines();
}
