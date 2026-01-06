package com.uth.confms.conference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConferenceTrackDTO {
    
    private Long id;
    
    @NotNull(message = "Conference ID is required")
    private Long conferenceId;
    
    @NotBlank(message = "Track name is required")
    private String name;
    
    private String description;
    
    private Integer displayOrder;
    
    private Boolean isActive;
}

