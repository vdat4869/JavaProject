package com.uth.confms.ai.entity;

import com.uth.confms.ai.enums.AIFeature;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ audit log cho mọi lời gọi AI.
 * Đảm bảo tính minh bạch và có thể kiểm tra lại.
 */
@Entity
@Table(name = "ai_audit_logs", indexes = {
        @Index(name = "idx_ai_audit_user", columnList = "userId"),
        @Index(name = "idx_ai_audit_conference", columnList = "conferenceId"),
        @Index(name = "idx_ai_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID của user thực hiện request */
    @Column(nullable = false)
    private Long userId;

    /** ID của conference (nullable nếu không liên quan đến conference cụ thể) */
    private Long conferenceId;

    /** Tính năng AI được sử dụng */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AIFeature aiFeature;

    /** Model AI được sử dụng (e.g., gpt-4o-mini) */
    @Column(nullable = false)
    private String modelIdentifier;

    /** Thời điểm request */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Hash của input (để bảo mật, không lưu nội dung gốc) */
    @Column(nullable = false)
    private String inputHash;

    /** Hash của output */
    @Column(nullable = false)
    private String outputHash;

    /** User đã chấp nhận gợi ý của AI chưa (null = chưa phản hồi) */
    private Boolean userAccepted;

    /** Thời gian xử lý (ms) */
    private Long processingTimeMs;

    /** Số tokens đã sử dụng (nếu có) */
    private Integer tokensUsed;

    /** Ghi chú lỗi nếu có */
    @Column(length = 500)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
