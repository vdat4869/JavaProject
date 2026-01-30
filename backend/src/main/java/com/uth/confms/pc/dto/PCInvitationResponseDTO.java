package com.uth.confms.pc.dto;

import java.time.LocalDateTime;

public class PCInvitationResponseDTO {
  private Long id;
  private Long conferenceId;
  private String invitedUserEmail; // Email người được mời
  private String status; // Trạng thái (PENDING, ACCEPTED,...)
  private LocalDateTime expiresAt; // Thời gian hết hạn
  private LocalDateTime createdAt;

  public PCInvitationResponseDTO() {
  }

  public PCInvitationResponseDTO(
      Long id,
      Long conferenceId,
      String invitedUserEmail,
      String status,
      LocalDateTime expiresAt,
      LocalDateTime createdAt) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.invitedUserEmail = invitedUserEmail;
    this.status = status;
    this.expiresAt = expiresAt;
    this.createdAt = createdAt;
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

  public String getInvitedUserEmail() {
    return invitedUserEmail;
  }

  public void setInvitedUserEmail(String invitedUserEmail) {
    this.invitedUserEmail = invitedUserEmail;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
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

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private String invitedUserEmail;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder invitedUserEmail(String invitedUserEmail) {
      this.invitedUserEmail = invitedUserEmail;
      return this;
    }

    public Builder status(String status) {
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

    public PCInvitationResponseDTO build() {
      return new PCInvitationResponseDTO(id, conferenceId, invitedUserEmail, status, expiresAt, createdAt);
    }
  }
}
