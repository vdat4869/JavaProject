package com.uth.confms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank
  @Email
  private String email; // Địa chỉ email đăng ký

  @NotBlank
  @PasswordConstraint
  private String password; // Mật khẩu

  @NotBlank
  private String firstName; // Tên

  @NotBlank
  private String lastName; // Họ

  private Long organizationId; // ID tổ chức

  private String phone; // Số điện thoại
}
