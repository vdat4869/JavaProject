package com.uth.confms.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RebuttalSubmitDTO {
    @NotNull(message = "Submission ID is required")
    private Long submissionId;
    
    @NotBlank(message = "Content is required")
    private String content;
}

