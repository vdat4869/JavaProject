package com.uth.confms.pc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PCInviteDTO {
  @NotNull(message = "Conference ID is required")
  private Long conferenceId;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
