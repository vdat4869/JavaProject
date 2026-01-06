package com.uth.confms.conference.dto;

import com.uth.confms.conference.entity.SubmissionForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFormDTO {
    
    private Long id;
    
    @NotNull(message = "Conference ID is required")
    private Long conferenceId;
    
    @NotBlank(message = "Field name is required")
    private String fieldName;
    
    @NotBlank(message = "Field label is required")
    private String fieldLabel;
    
    @NotNull(message = "Field type is required")
    private SubmissionForm.FieldType fieldType;
    
    private Map<String, Object> fieldOptions;
    
    private Boolean isRequired;
    
    private Integer displayOrder;
    
    private Map<String, Object> validationRules;
}

