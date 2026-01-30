package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.UserDTO;
import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các request liên quan đến User management
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Lấy thông tin user hiện tại
   *
   * @param authentication Authentication object
   * @return ApiResponse chứa UserDTO của user hiện tại
   */
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    UserDTO user = userService.getUserById(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  /**
   * Lấy thông tin user theo ID (chỉ admin)
   *
   * @param id ID của user
   * @return ApiResponse chứa UserDTO
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
    UserDTO user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  /**
   * Cập nhật thông tin profile của user hiện tại
   *
   * @param authentication Authentication object
   * @param userDTO        Thông tin mới
   * @return ApiResponse chứa UserDTO sau khi cập nhật
   */
  @PutMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(
      Authentication authentication, @RequestBody UserDTO userDTO) {
    Long userId = getUserIdFromAuthentication(authentication);
    UserDTO updated = userService.updateUser(userId, userDTO);
    return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
  }

  /**
   * Lấy danh sách tất cả user (chỉ admin)
   *
   * @param page Trang (mặc định 0)
   * @param size Kích thước trang (mặc định 20)
   * @return ApiResponse chứa Page của UserDTO
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'CHAIR')")
  public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<UserDTO> users = userService.getAllUsers(pageable);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  /**
   * Lấy danh sách user đang active (chỉ admin)
   *
   * @param page Trang (mặc định 0)
   * @param size Kích thước trang (mặc định 20)
   * @return ApiResponse chứa Page của UserDTO
   */
  @GetMapping("/active/list")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<UserDTO>>> getActiveUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<UserDTO> users = userService.getAllActiveUsers(pageable);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  /**
   * Tìm kiếm user theo tên hoặc email (chỉ admin)
   *
   * @param keyword Từ khóa tìm kiếm
   * @param page    Trang (mặc định 0)
   * @param size    Kích thước trang (mặc định 20)
   * @return ApiResponse chứa Page của UserDTO matching criteria
   */
  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('ADMIN', 'CHAIR')")
  public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    if (keyword == null || keyword.isBlank()) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Keyword is required"));
    }
    Pageable pageable = PageRequest.of(page, size);
    Page<UserDTO> users = userService.searchUsers(keyword, pageable);
    return ResponseEntity.ok(ApiResponse.success(users));
  }

  /**
   * Khóa tài khoản người dùng (chỉ ADMIN).
   */
  @PutMapping("/{id}/deactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
    userService.deactivateUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
  }

  /**
   * Kích hoạt lại tài khoản người dùng (chỉ ADMIN).
   */
  @PutMapping("/{id}/activate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
    userService.activateUser(id);
    return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
  }

  /**
   * Cập nhật quyền (roles) cho user (chỉ admin)
   *
   * @param id    ID của user cần cập nhật
   * @param roles Tập hợp các tên role mới
   * @return ApiResponse xác nhận cập nhật thành công
   */
  @PutMapping("/{id}/roles")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> updateRoles(
      @PathVariable Long id, @RequestBody Set<String> roles) {
    userService.updateUserRoles(id, roles);
    return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", null));
  }

  /**
   * Lấy thống kê về user
   *
   * @return ApiResponse chứa thống kê
   */
  @GetMapping("/stats/summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Map<String, Long>>> getUserStats() {
    long activeCount = userService.countActiveUsers();
    long verifiedCount = userService.countVerifiedUsers();

    Map<String, Long> stats = new HashMap<>();
    stats.put("activeUsers", activeCount);
    stats.put("verifiedUsers", verifiedCount);

    return ResponseEntity.ok(ApiResponse.success(stats));
  }

  /**
   * Extract user ID from Authentication object
   *
   * @param authentication Authentication object
   * @return User ID
   */
  private Long getUserIdFromAuthentication(Authentication authentication) {
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
