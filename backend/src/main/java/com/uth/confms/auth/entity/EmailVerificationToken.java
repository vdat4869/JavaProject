package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "email_verification_tokens")
@EntityListeners(AuditingEntityListener.class)
public class EmailVerificationToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private Boolean used = false;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public EmailVerificationToken() {
    this.used = false;
  }

  public EmailVerificationToken(
      Long id,
      String token,
      User user,
      LocalDateTime expiresAt,
      Boolean used,
      LocalDateTime createdAt) {
    this.id = id;
    this.token = token;
    this.user = user;
    this.expiresAt = expiresAt;
    this.used = used != null ? used : false;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Boolean getUsed() {
    return used;
  }

  public void setUsed(Boolean used) {
    this.used = used;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private String token;
    private User user;
    private LocalDateTime expiresAt;
    private Boolean used = false;
    private LocalDateTime createdAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder token(String token) {
      this.token = token;
      return this;
    }

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public Builder expiresAt(LocalDateTime expiresAt) {
      this.expiresAt = expiresAt;
      return this;
    }

    public Builder used(Boolean used) {
      this.used = used != null ? used : false;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public EmailVerificationToken build() {
      return new EmailVerificationToken(id, token, user, expiresAt, used, createdAt);
    }
  }
}
