package com.uth.confms.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request tạo assignment mới
 * 
 * <p>DTO này được sử dụng khi chair muốn assign một reviewer cho submission.
 * 
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Data
public class AssignmentCreateDTO {
    @NotNull(message = "Submission ID is required")
    private Long submissionId;
    
    @NotNull(message = "Reviewer ID is required")
    private Long reviewerId;
    
    private Boolean isPrimary = false;
}

