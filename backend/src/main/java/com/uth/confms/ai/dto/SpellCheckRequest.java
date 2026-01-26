package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO cho tính năng kiểm tra chính tả.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpellCheckRequest {

    private Long conferenceId;

    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    @NotBlank(message = "Abstract is required")
    @Size(max = 5000, message = "Abstract cannot exceed 5000 characters")
    private String abstractText;

    private String keywords;

    /** Ngôn ngữ (en, vi) */
    @Builder.Default
    private String language = "en";
}
