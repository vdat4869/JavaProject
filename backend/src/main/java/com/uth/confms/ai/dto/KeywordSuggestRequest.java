package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO cho tính năng gợi ý keywords.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordSuggestRequest {

    private Long conferenceId;

    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    @NotBlank(message = "Abstract is required")
    @Size(max = 5000, message = "Abstract cannot exceed 5000 characters")
    private String abstractText;

    /** Keywords hiện tại (nếu có) để AI không lặp lại */
    private String existingKeywords;

    /** Số lượng keywords mong muốn */
    @Builder.Default
    private Integer maxKeywords = 5;
}
