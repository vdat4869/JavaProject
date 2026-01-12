package com.uth.confms.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String affiliation;
  private String phone;
  private Boolean emailVerified;
  private Boolean active;
  private Set<String> roles;
  private LocalDateTime createdAt;
}
