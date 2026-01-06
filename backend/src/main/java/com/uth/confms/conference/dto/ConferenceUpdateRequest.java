package com.uth.confms.conference.dto;

import com.uth.confms.conference.entity.Conference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenceUpdateRequest {
    
    private String name;
    
    private String description;
    
    private String acronym;
    
    private Integer year;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDateTime submissionDeadline;
    
    private LocalDateTime reviewDeadline;
    
    private LocalDateTime cameraReadyDeadline;
    
    private Conference.ReviewMode reviewMode;
    
    private Conference.ConferenceStatus status;
}

