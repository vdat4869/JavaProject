package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token_user", columnList = "user_id"),
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hash của refresh token (KHÔNG lưu token gốc)
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 512)
    private String tokenHash;

    /**
     * User sở hữu refresh token
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Thời điểm hết hạn
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Token đã bị revoke hay chưa
     */
    @Column(nullable = false)
    private boolean revoked;

    /**
     * Thông tin thiết bị (Chrome / iPhone / Android…)
     */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    /**
     * IP tại thời điểm login
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Thời điểm tạo token
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời điểm token bị revoke (logout)
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.revoked = false;
    }
}
