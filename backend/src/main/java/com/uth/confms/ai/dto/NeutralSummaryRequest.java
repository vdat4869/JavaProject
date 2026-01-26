package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO cho tính năng tạo tóm tắt trung lập.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NeutralSummaryRequest {

    private Long conferenceId;

    @NotBlank(message = "Abstract is required")
    @Size(max = 5000, message = "Abstract cannot exceed 5000 characters")
    private String abstractText;

    /** Độ dài mong muốn (số từ) */
    @Builder.Default
    private Integer targetWordCount = 200;
}
