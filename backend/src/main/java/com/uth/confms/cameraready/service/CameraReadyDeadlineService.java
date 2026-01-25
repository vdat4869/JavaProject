package com.uth.confms.cameraready.service;

import java.util.UUID;

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
    void checkAndCloseDeadline(UUID conferenceId);
    
    /**
     * Send reminder notifications trước deadline
     * 
     * @param conferenceId Conference ID
     * @param daysBeforeDeadline Số ngày trước deadline để gửi reminder
     */
    void sendDeadlineReminders(UUID conferenceId, int daysBeforeDeadline);
    
    /**
     * Check tất cả conferences có deadline đã hết
     */
    void checkAllDeadlines();
}
