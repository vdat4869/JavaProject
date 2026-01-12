package com.uth.confms.auth.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
  private String accessToken;
  private String refreshToken;

  @Builder.Default private String tokenType = "Bearer";

  private Long userId;
  private String email;
  private String fullName;
  private Set<String> roles;
  private Boolean emailVerified;
}
