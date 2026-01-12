package com.uth.confms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank @Email private String email;

  @NotBlank
  @Size(min = 8)
  private String password;

  @NotBlank private String firstName;

  @NotBlank private String lastName;

  private String affiliation;

  private String phone;
}
