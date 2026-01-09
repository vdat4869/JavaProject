package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
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

  public AuditLog() {}

  public AuditLog(
      Long id,
      Long userId,
      String username,
      String action,
      String resource,
      Long resourceId,
      String details,
      String ipAddress,
      String userAgent,
      LocalDateTime timestamp) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.action = action;
    this.resource = resource;
    this.resourceId = resourceId;
    this.details = details;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.timestamp = timestamp;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public void setResourceId(Long resourceId) {
    this.resourceId = resourceId;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String resource;
    private Long resourceId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder action(String action) {
      this.action = action;
      return this;
    }

    public Builder resource(String resource) {
      this.resource = resource;
      return this;
    }

    public Builder resourceId(Long resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder details(String details) {
      this.details = details;
      return this;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder userAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public Builder timestamp(LocalDateTime timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public AuditLog build() {
      return new AuditLog(
          id, userId, username, action, resource, resourceId, details, ipAddress, userAgent,
          timestamp);
    }
  }
}
