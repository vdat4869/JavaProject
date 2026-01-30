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
  private Long id; // ID người dùng
  private String email; // Email
  private String firstName; // Tên
  private String lastName; // Họ
  private Long organizationId; // ID tổ chức
  private String organizationName; // Tên tổ chức
  private String phone; // Số điện thoại
  private Boolean emailVerified; // Đã xác thực email chưa
  private Boolean active; // Trạng thái hoạt động
  private Set<String> roles; // Các vai trò của user
  private LocalDateTime createdAt; // Ngày tạo
  private LocalDateTime updatedAt; // Ngày cập nhật
}
