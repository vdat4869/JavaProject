package com.uth.confms.conference.dto;

import com.uth.confms.conference.entity.EmailTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDTO {
    
    private Long id;
    
    @NotNull(message = "Conference ID is required")
    private Long conferenceId;
    
    @NotNull(message = "Template type is required")
    private EmailTemplate.TemplateType templateType;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Body is required")
    private String body;
    
    private List<String> variables;
    
    private Boolean isActive;
}

