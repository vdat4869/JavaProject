package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.LoginRequest;
import com.uth.confms.auth.dto.LoginResponse;
import com.uth.confms.auth.dto.SignupRequest;
import com.uth.confms.auth.dto.VerifyEmailRequest;
import com.uth.confms.auth.service.AuthService;
import com.uth.confms.auth.service.TokenService;
import com.uth.confms.common.annotations.NoAuth;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.email.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý các request liên quan đến xác thực (authentication)
 *
 * <p>Các endpoints:
 *
 * <ul>
 *   <li>POST /api/auth/signup - Đăng ký tài khoản mới
 *   <li>POST /api/auth/login - Đăng nhập
 *   <li>POST /api/auth/refresh - Refresh access token
 *   <li>POST /api/auth/verify-email - Xác thực email
 *   <li>POST /api/auth/resend-verification - Gửi lại email verification
 *   <li>POST /api/auth/logout - Đăng xuất
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final TokenService tokenService;
  @SuppressWarnings("unused")
  private final EmailVerificationService emailVerificationService;

  public AuthController(AuthService authService, TokenService tokenService, EmailVerificationService emailVerificationService) {
    this.authService = authService;
    this.tokenService = tokenService;
    this.emailVerificationService = emailVerificationService;
  }

  /**
   * Đăng ký tài khoản mới
   *
   * @param request Thông tin đăng ký (email, password, firstName, lastName, etc.)
   * @return ApiResponse chứa LoginResponse với access token và refresh token
   */
  @PostMapping("/signup")
  @NoAuth
  public ResponseEntity<ApiResponse<LoginResponse>> signup(
      @Valid @RequestBody SignupRequest request) {
    LoginResponse response = authService.signup(request);
    return ResponseEntity.ok(
        ApiResponse.success("Registration successful. You can now login.", response));
  }

  /**
   * Đăng nhập vào hệ thống
   *
   * @param request Thông tin đăng nhập (email, password)
   * @return ApiResponse chứa LoginResponse với access token và refresh token
   */
  @PostMapping("/login")
  @NoAuth
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /**
   * Refresh access token bằng refresh token
   *
   * @param refreshToken Refresh token từ header Authorization
   * @return ApiResponse chứa LoginResponse với access token mới
   */
  @PostMapping("/refresh")
  @NoAuth
  public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
      @RequestHeader("Authorization") String refreshToken) {
    String token = refreshToken.replace("Bearer ", "");
    String newAccessToken = tokenService.refreshAccessToken(token);

    LoginResponse response =
        LoginResponse.builder().accessToken(newAccessToken).tokenType("Bearer").build();

    return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
  }

  /**
   * Xác thực email bằng token
   * 
   * @deprecated Email verification is disabled. This endpoint is kept for backward compatibility.
   * @param request Chứa verification token
   * @return ApiResponse xác nhận email đã được verify
   */
  @Deprecated
  @PostMapping("/verify-email")
  @NoAuth
  public ResponseEntity<ApiResponse<Void>> verifyEmail(
      @Valid @RequestBody VerifyEmailRequest request) {
    // Email verification is disabled - always return success
    return ResponseEntity.ok(ApiResponse.success("Email verification is disabled", null));
  }

  /**
   * Gửi lại email verification
   * 
   * @deprecated Email verification is disabled. This endpoint is kept for backward compatibility.
   * @param email Email cần gửi lại verification
   * @return ApiResponse xác nhận email đã được gửi
   */
  @Deprecated
  @PostMapping("/resend-verification")
  @NoAuth
  public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
    // Email verification is disabled - always return success
    return ResponseEntity.ok(ApiResponse.success("Email verification is disabled", null));
  }

  /**
   * Đăng xuất khỏi hệ thống
   *
   * <p>Lưu ý: Token invalidation được xử lý bởi frontend
   *
   * @return ApiResponse xác nhận đã logout
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout() {
    // Token invalidation handled by frontend
    return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
  }
}
