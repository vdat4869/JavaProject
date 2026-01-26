package com.uth.confms.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO cho tính năng soạn email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDraftRequest {

    private Long conferenceId;

    /** Loại email: DECISION, REMINDER, INVITATION, NOTIFICATION */
    @NotBlank(message = "Email type is required")
    private String emailType;

    /** Context/thông tin cần đưa vào email */
    private String context;

    /** Tên hội nghị */
    private String conferenceName;

    /** Tên người nhận (hoặc placeholder) */
    @Builder.Default
    private String recipientName = "[Recipient Name]";

    /** Tone: formal, friendly, urgent */
    @Builder.Default
    private String tone = "formal";

    /** Ngôn ngữ: en, vi */
    @Builder.Default
    private String language = "en";
}
