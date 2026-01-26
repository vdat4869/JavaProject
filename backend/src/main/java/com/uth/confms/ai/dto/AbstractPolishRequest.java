package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO cho tính năng polish abstract.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbstractPolishRequest {

    private Long conferenceId;

    @NotBlank(message = "Abstract is required")
    @Size(max = 5000, message = "Abstract cannot exceed 5000 characters")
    private String abstractText;

    /** Tone mong muốn: formal, concise, expanded */
    @Builder.Default
    private String tone = "formal";

    /** Ngôn ngữ (en, vi) */
    @Builder.Default
    private String language = "en";
}
