package com.uth.confms.pc.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "pc_invitations")
@EntityListeners(AuditingEntityListener.class)
public class PCInvitation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long conferenceId;

  @Column(nullable = false)
  private Long invitedUserId; // Người được mời

  @Column(nullable = false)
  private Long invitedBy; // Người mời (Chair)

  @Column(nullable = false, unique = true)
  private String token; // Token xác thực trong email

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private InvitationStatus status = InvitationStatus.PENDING;

  @Column(nullable = false)
  private LocalDateTime expiresAt; // Thời gian hết hạn

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public PCInvitation() {
  }

  public PCInvitation(
      Long id,
      Long conferenceId,
      Long invitedUserId,
      Long invitedBy,
      String token,
      InvitationStatus status,
      LocalDateTime expiresAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.invitedUserId = invitedUserId;
    this.invitedBy = invitedBy;
    this.token = token;
    this.status = status;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public Long getInvitedUserId() {
    return invitedUserId;
  }

  public void setInvitedUserId(Long invitedUserId) {
    this.invitedUserId = invitedUserId;
  }

  public Long getInvitedBy() {
    return invitedBy;
  }

  public void setInvitedBy(Long invitedBy) {
    this.invitedBy = invitedBy;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public InvitationStatus getStatus() {
    return status;
  }

  public void setStatus(InvitationStatus status) {
    this.status = status;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public enum InvitationStatus {
    PENDING, // Đang chờ
    ACCEPTED, // Đã chấp nhận
    DECLINED, // Đã từ chối
    EXPIRED // Hết hạn
  }

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private Long invitedUserId;
    private Long invitedBy;
    private String token;
    private InvitationStatus status = InvitationStatus.PENDING;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder invitedUserId(Long invitedUserId) {
      this.invitedUserId = invitedUserId;
      return this;
    }

    public Builder invitedBy(Long invitedBy) {
      this.invitedBy = invitedBy;
      return this;
    }

    public Builder token(String token) {
      this.token = token;
      return this;
    }

    public Builder status(InvitationStatus status) {
      this.status = status;
      return this;
    }

    public Builder expiresAt(LocalDateTime expiresAt) {
      this.expiresAt = expiresAt;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public PCInvitation build() {
      return new PCInvitation(
          id, conferenceId, invitedUserId, invitedBy, token, status, expiresAt, createdAt, updatedAt);
    }
  }
}
