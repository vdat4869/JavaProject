package com.uth.confms.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity để track SMTP quota usage
 */
@Entity
@Table(name = "email_quota", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "quota_date", "quota_type" })
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate quotaDate; // Date for daily quota tracking

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuotaType quotaType;

    @Column(nullable = false)
    @Builder.Default
    private Long emailsSent = 0L; // Number of emails sent

    @Column(nullable = false)
    private Long quotaLimit; // Maximum emails allowed (per day/hour)

    @Column(nullable = false)
    @Builder.Default
    private Boolean quotaExceeded = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum QuotaType {
        DAILY, // Daily quota limit
        HOURLY // Hourly quota limit
    }

    /**
     * Check if quota is exceeded
     */
    public boolean isQuotaExceeded() {
        return emailsSent >= quotaLimit;
    }

    /**
     * Get remaining quota
     */
    public Long getRemainingQuota() {
        return Math.max(0, quotaLimit - emailsSent);
    }
}
