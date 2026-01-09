package com.uth.confms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
  @NotBlank private String currentPassword;

  @NotBlank
  @Size(min = 8)
  private String newPassword;

  public ChangePasswordRequest() {}

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
}
