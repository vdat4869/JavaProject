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
public class UserDTO {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private Long organizationId;
  private String organizationName;
  private String phone;
  private Boolean emailVerified;
  private Boolean active;
  private Set<String> roles;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
