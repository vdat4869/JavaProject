package com.uth.confms.pc.dto;

import java.time.LocalDateTime;

public class PCMemberDTO {
  private Long id;
  private Long conferenceId;
  private Long userId;
  private String email;
  private String fullName;
  private String status; // Trạng thái thành viên
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public PCMemberDTO() {
  }

  public PCMemberDTO(
      Long id,
      Long conferenceId,
      Long userId,
      String email,
      String fullName,
      String status,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
    this.status = status;
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

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private Long userId;
    private String email;
    private String fullName;
    private String status;
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

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder fullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
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

    public PCMemberDTO build() {
      return new PCMemberDTO(id, conferenceId, userId, email, fullName, status, createdAt, updatedAt);
    }
  }
}
