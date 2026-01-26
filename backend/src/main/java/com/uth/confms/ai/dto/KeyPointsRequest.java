package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO cho tính năng trích xuất key points.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyPointsRequest {

    private Long conferenceId;

    @NotBlank(message = "Abstract is required")
    @Size(max = 5000, message = "Abstract cannot exceed 5000 characters")
    private String abstractText;
}
