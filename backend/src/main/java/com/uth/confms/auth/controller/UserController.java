package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.UserDTO;
import com.uth.confms.auth.service.UserService;
import com.uth.confms.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
    Long userId = getUserIdFromAuthentication(authentication);
    UserDTO user = userService.getUserById(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
    UserDTO user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @PutMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserDTO>> updateCurrentUser(
      Authentication authentication, @RequestBody UserDTO userDTO) {
    Long userId = getUserIdFromAuthentication(authentication);
    UserDTO updated = userService.updateUser(userId, userDTO);
    return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    // Extract user ID from authentication
    // This is a placeholder - implement based on your JWT structure
    String email = authentication.getName();
    return userService.getUserIdByEmail(email);
  }
}
