package com.uth.confms.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity để queue failed emails cho retry
 */
@Entity
@Table(name = "email_queue")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String templateName; // Template name if using template

    @Column(columnDefinition = "TEXT")
    private String content; // Plain text content or rendered HTML

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    @Column
    private LocalDateTime nextRetryAt; // When to retry next

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // Last error message

    @Column
    private LocalDateTime sentAt; // When email was successfully sent

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum EmailStatus {
        PENDING, // Waiting to be sent
        PROCESSING, // Currently being processed
        SENT, // Successfully sent
        FAILED, // Failed after max retries
        CANCELLED // Cancelled manually
    }

    /**
     * Check if email can be retried
     */
    public boolean canRetry() {
        return status == EmailStatus.PENDING ||
                (status == EmailStatus.PROCESSING && retryCount < maxRetries);
    }

    /**
     * Increment retry count
     */
    public void incrementRetry() {
        this.retryCount++;
        if (this.retryCount >= this.maxRetries) {
            this.status = EmailStatus.FAILED;
        }
    }
}
