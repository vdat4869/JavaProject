package com.uth.confms.cameraready.service.impl;

import com.uth.confms.cameraready.service.CameraReadyDeadlineService;
import com.uth.confms.cameraready.service.CameraReadyService;
import com.uth.confms.conference.entity.Deadline;
import com.uth.confms.conference.repository.DeadlineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation của CameraReadyDeadlineService.
 * 
 * @author UTH-ConfMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CameraReadyDeadlineServiceImpl implements CameraReadyDeadlineService {
    
    private final CameraReadyService cameraReadyService;
    private final DeadlineRepository deadlineRepository;
    
    @Override
    @Transactional
    public void checkAndCloseDeadline(UUID conferenceId) {
        log.debug("Checking deadline for conference {}", conferenceId);
        
        // Convert UUID to Long for deadline lookup
        Long conferenceIdLong = convertUUIDToLong(conferenceId);
        
        // Find CAMERA_READY deadline
        List<Deadline> deadlines = deadlineRepository.findByConferenceId(conferenceIdLong);
        Optional<Deadline> cameraReadyDeadline = deadlines.stream()
                .filter(d -> d.getType() == Deadline.DeadlineType.CAMERA_READY)
                .findFirst();
        
        if (cameraReadyDeadline.isPresent()) {
            Deadline deadline = cameraReadyDeadline.get();
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(deadline.getDueDate()) && deadline.getHardDeadline()) {
                log.info("Camera-ready deadline has passed for conference {}, auto-closing", conferenceId);
                cameraReadyService.closeCameraReady(conferenceId, "Deadline đã hết hạn", null);
            }
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public void sendDeadlineReminders(UUID conferenceId, int daysBeforeDeadline) {
        log.info("Sending deadline reminders for conference {} ({} days before)", conferenceId, daysBeforeDeadline);
        
        // Convert UUID to Long for deadline lookup
        Long conferenceIdLong = convertUUIDToLong(conferenceId);
        
        // Find CAMERA_READY deadline
        List<Deadline> deadlines = deadlineRepository.findByConferenceId(conferenceIdLong);
        Optional<Deadline> cameraReadyDeadline = deadlines.stream()
                .filter(d -> d.getType() == Deadline.DeadlineType.CAMERA_READY)
                .findFirst();
        
        if (cameraReadyDeadline.isPresent()) {
            Deadline deadline = cameraReadyDeadline.get();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderDate = deadline.getDueDate().minusDays(daysBeforeDeadline);
            
            // Check if we're in the reminder window (within 1 day of reminder date)
            if (now.isAfter(reminderDate) && now.isBefore(deadline.getDueDate())) {
                log.info("Reminder window active for conference {}", conferenceId);
                // TODO: Send email reminders to authors
                // This would require integration with email service
            }
        }
    }
    
    @Override
    @Transactional
    public void checkAllDeadlines() {
        log.info("Checking all camera-ready deadlines");
        // This would require getting all conferences with camera-ready phase open
        // For now, this is a placeholder
        // In production, this would be called by a scheduled task
    }
    
    /**
     * Convert UUID to Long (simplified conversion)
     */
    private Long convertUUIDToLong(UUID uuid) {
        long mostSignificantBits = uuid.getMostSignificantBits();
        return Math.abs(mostSignificantBits % Long.MAX_VALUE);
    }
}
