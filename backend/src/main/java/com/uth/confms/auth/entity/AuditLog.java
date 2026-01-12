package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.*;

/**
 * Entity lưu nhật ký hành động của người dùng trong hệ thống
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  private String username;

  @Column(nullable = false)
  private String action;

  private String resource;

  private Long resourceId;

  private String details;

  private String ipAddress;

  private String userAgent;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime timestamp;
}
