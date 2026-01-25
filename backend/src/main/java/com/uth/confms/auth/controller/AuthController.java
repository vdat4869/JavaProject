package com.uth.confms.auth.controller;

import com.uth.confms.auth.dto.ChangePasswordRequest;
import com.uth.confms.auth.dto.LoginRequest;
import com.uth.confms.auth.dto.LoginResponse;
import com.uth.confms.auth.dto.RegisterRequest;
import com.uth.confms.auth.service.AuthService;
import com.uth.confms.auth.service.TokenService;
import com.uth.confms.auth.repository.RefreshTokenRepository;
import com.uth.confms.common.annotations.NoAuth;
import com.uth.confms.common.dto.ApiResponse;
import com.uth.confms.common.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller xử lý các request liên quan đến xác thực (authentication)
 *
 * <p>
 * Các endpoints:
 *
 * <ul>
 * <li>POST /api/auth/register - Đăng ký tài khoản mới
 * <li>POST /api/auth/login - Đăng nhập
 * <li>POST /api/auth/refresh - Refresh access token
 * <li>POST /api/auth/logout - Đăng xuất
 * <li>GET /api/auth/sso/redirect - Lấy SSO redirect URL
 * <li>POST /api/auth/sso/callback - Xử lý SSO callback
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
  private final RefreshTokenRepository refreshTokenRepository;
  private final com.uth.confms.auth.service.UserService userService;

  @Value("${oauth2.enabled:true}")
  private boolean oauth2Enabled;

  @Value("${spring.security.oauth2.client.registration.google.client-id:}")
  private String googleClientId;

  @Value("${oauth2.redirect-uri:http://localhost:8080/login/oauth2/code/google}")
  private String oauth2RedirectUri;

  public AuthController(AuthService authService, TokenService tokenService,
      RefreshTokenRepository refreshTokenRepository,
      com.uth.confms.auth.service.UserService userService) {
    this.authService = authService;
    this.tokenService = tokenService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userService = userService;
  }

  /**
   * Đăng ký tài khoản mới
   *
   * @param request Thông tin đăng ký (email, password, firstName, lastName, etc.)
   * @return ApiResponse chứa LoginResponse với access token và refresh token
   */
  @PostMapping("/register")
  @NoAuth
  public ResponseEntity<ApiResponse<LoginResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    LoginResponse response = authService.register(request);
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
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {
    LoginResponse response = authService.login(request, httpRequest);
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
      @RequestHeader("Authorization") String refreshToken,
      HttpServletRequest httpRequest) {
    String token = refreshToken.replace("Bearer ", "");
    String newAccessToken = tokenService.refreshAccessToken(token, httpRequest);

    LoginResponse response = LoginResponse.builder().accessToken(newAccessToken).tokenType("Bearer").build();

    return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
  }

  /**
   * Đăng xuất khỏi hệ thống
   *
   * <p>
   * Lưu ý: Token invalidation được xử lý bởi frontend
   *
   * @return ApiResponse xác nhận đã logout
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader(name = "Authorization", required = false) String authorization,
      HttpServletRequest httpRequest) {
    if (authorization != null && authorization.startsWith("Bearer ")) {
      String token = authorization.replace("Bearer ", "");
      try {
        // compute hash and revoke the refresh token record
        String tokenHash = sha256Hex(token);
        refreshTokenRepository.revokeByTokenHash(tokenHash, java.time.LocalDateTime.now());
        // Logout through AuthService for audit logging
        authService.logout(token, httpRequest);
      } catch (Exception ignored) {
        // ignore
      }
    }

    return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
  }

  /**
   * Change password for current user
   *
   * @param authentication Authentication object
   * @param request        Change password request (currentPassword, newPassword)
   * @param httpRequest    HTTP request
   * @return ApiResponse confirming password change
   */
  @PostMapping("/change-password")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      Authentication authentication,
      @Valid @RequestBody ChangePasswordRequest request,
      HttpServletRequest httpRequest) {
    Long userId = getUserIdFromAuthentication(authentication);
    authService.changePassword(userId, request, httpRequest);
    return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
  }

  /**
   * Lấy SSO redirect URL cho Google OAuth2
   *
   * @return URL để redirect user đến Google sign-in
   */
  @GetMapping("/sso/redirect")
  @NoAuth
  public ResponseEntity<ApiResponse<Map<String, String>>> getSSORedirectUrl() {
    if (!oauth2Enabled) {
      throw new BusinessException("SSO is not enabled");
    }
    if (googleClientId == null || googleClientId.isEmpty()) {
      throw new BusinessException("Google OAuth2 is not configured");
    }

    String redirectUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
        "?client_id=" + googleClientId +
        "&redirect_uri=" + java.net.URLEncoder.encode(oauth2RedirectUri, java.nio.charset.StandardCharsets.UTF_8) +
        "&response_type=code" +
        "&scope=openid%20email%20profile" +
        "&access_type=offline";

    return ResponseEntity.ok(ApiResponse.success(Map.of("redirectUrl", redirectUrl)));
  }

  /**
   * Xử lý SSO callback sau khi user authenticate với Google
   *
   * @param code  Authorization code từ Google
   * @param state Optional state parameter
   * @return LoginResponse với tokens
   */
  @PostMapping("/sso/callback")
  @NoAuth
  public ResponseEntity<ApiResponse<LoginResponse>> handleSSOCallback(
      @RequestParam String code,
      @RequestParam(required = false) String state,
      HttpServletRequest httpRequest) {
    if (!oauth2Enabled) {
      throw new BusinessException("SSO is not enabled");
    }

    // TODO: Implement actual OAuth2 token exchange and user creation
    // For now, throw error indicating SSO needs to be configured
    throw new BusinessException("SSO callback not yet implemented. Please use email/password login.");
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

  private static String sha256Hex(String input) {
    try {
      java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }
}
