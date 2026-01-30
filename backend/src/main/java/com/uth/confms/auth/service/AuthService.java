package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.*;
import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.entity.RefreshToken;
import com.uth.confms.auth.entity.PasswordResetToken;
import com.uth.confms.auth.enums.RoleName;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.RoleRepository;
import com.uth.confms.auth.repository.RefreshTokenRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.auth.repository.PasswordResetTokenRepository;
import com.uth.confms.email.service.EmailService;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.auth.enums.LoginProvider;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý xác thực và đăng nhập/đăng ký người dùng
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  @SuppressWarnings("unused")
  private final UserDetailsService userDetailsService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final AuditLogService auditLogService;
  private final GoogleTokenService googleTokenService;
  private final EmailService emailService;
  private final com.uth.confms.common.repository.OrganizationRepository organizationRepository;

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      RefreshTokenRepository refreshTokenRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      AuditLogService auditLogService,
      GoogleTokenService googleTokenService,
      EmailService emailService,
      com.uth.confms.common.repository.OrganizationRepository organizationRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.auditLogService = auditLogService;
    this.googleTokenService = googleTokenService;
    this.emailService = emailService;
    this.organizationRepository = organizationRepository;
  }

  /**
   * Đăng ký tài khoản mới cho người dùng
   *
   * @param request Thông tin đăng ký (email, password, firstName, lastName, etc.)
   * @return LoginResponse chứa access token, refresh token và thông tin user
   * @throws BusinessException Nếu email đã tồn tại
   * @throws NotFoundException Nếu không tìm thấy role AUTHOR
   */
  @Transactional
  public LoginResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException("Email already exists", "EMAIL_EXISTS");
    }

    com.uth.confms.common.entity.Organization organization = null;
    if (request.getOrganizationId() != null) {
      organization = organizationRepository.findById(request.getOrganizationId()).orElse(null);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .organization(organization)
        .phone(request.getPhone())
        .emailVerified(true) // Email verification disabled - set to true by default
        .active(true)
        .build();

    // Get or create AUTHOR role
    Role authorRole = roleRepository
        .findByName(RoleName.AUTHOR)
        .orElseGet(
            () -> {
              Role newRole = Role.builder().name(RoleName.AUTHOR).description("Role: AUTHOR").build();
              @SuppressWarnings("null")
              Role savedRole = roleRepository.save(newRole);
              return savedRole;
            });
    user.getRoles().add(authorRole);

    user = userRepository.save(user);

    // Audit log: Registration
    try {
      auditLogService.logAction(
          user.getId(),
          user.getEmail(),
          "REGISTER",
          "AUTH",
          null,
          "User registered successfully",
          null);
    } catch (Exception e) {
      // Don't block registration if audit logging fails
    }

    // Generate tokens for immediate login after registration
    String accessToken = jwtService.generateAccessToken(user);
    String refreshTokenString = jwtService.generateRefreshToken(user);

    // Create RefreshToken entity
    try {
      java.util.Date exp = jwtService.extractExpiration(refreshTokenString);
      LocalDateTime expiresAt = exp.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();

      String tokenHash = sha256Hex(refreshTokenString);

      RefreshToken refreshToken = RefreshToken.builder()
          .tokenHash(tokenHash)
          .user(user)
          .expiresAt(expiresAt)
          .createdAt(LocalDateTime.now())
          .deviceInfo(null) // Registration doesn't have device info
          .ipAddress(null) // Registration doesn't have IP
          .revoked(false)
          .build();

      refreshTokenRepository.save(refreshToken);
    } catch (Exception e) {
      // Don't block registration if token saving fails
      log.warn("Failed to save refresh token during registration", e);
    }

    Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());

    return LoginResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .roles(roles)
        .emailVerified(user.getEmailVerified())
        .accessToken(accessToken)
        .refreshToken(refreshTokenString)
        .tokenType("Bearer")
        .build();
  }

  /**
   * Thay đổi mật khẩu người dùng.
   * Kiểm tra mật khẩu hiện tại trước khi cập nhật.
   */
  @Transactional
  public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      // Audit log: Password change failure
      try {
        auditLogService.logAction(
            userId,
            user.getEmail(),
            "PASSWORD_CHANGE_FAILED",
            "AUTH",
            null,
            "Current password is incorrect",
            httpRequest);
      } catch (Exception e) {
        // Don't block if audit logging fails
      }
      throw new UnauthorizedException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Audit log: Password change success
    try {
      auditLogService.logAction(
          userId,
          user.getEmail(),
          "PASSWORD_CHANGED",
          "AUTH",
          null,
          "Password changed successfully",
          httpRequest);
    } catch (Exception e) {
      // Don't block if audit logging fails
    }
  }

  /**
   * Đăng xuất người dùng.
   * Thu hồi (revoke) refresh token và ghi log hành động.
   */
  public void logout(String refreshToken, HttpServletRequest httpRequest) {
    String tokenHash = sha256Hex(refreshToken);

    // Try to get user from refresh token for audit logging BEFORE revoking
    try {
      // Find refresh token to get user (even if already revoked by controller,
      // but here we check before we might have repeated the logic)
      var refreshTokenOpt = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash);
      if (refreshTokenOpt.isPresent()) {
        var rt = refreshTokenOpt.get();
        User user = rt.getUser();
        if (user != null) {
          auditLogService.logAction(
              user.getId(),
              user.getEmail(),
              "LOGOUT",
              "AUTH",
              null,
              "User logged out successfully",
              httpRequest);
        }
      }
    } catch (Exception e) {
      log.warn("Failed to log logout action", e);
    }

    // Revoke the token (already called in AuthController, but good to have as
    // backup/service logic)
    refreshTokenRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
  }

  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  /**
   * Đăng nhập người dùng vào hệ thống
   *
   * @param request Thông tin đăng nhập (email, password)
   * @return LoginResponse chứa access token, refresh token và thông tin user
   * @throws UnauthorizedException Nếu email/password sai hoặc account bị disable
   * @throws NotFoundException     Nếu không tìm thấy user
   */
  public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    String email = request.getEmail();
    User user = null;

    // 1. Authenticate
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              email,
              request.getPassword()));
    } catch (BadCredentialsException e) {
      // Audit log: Login failure
      try {
        user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
          auditLogService.logAction(
              user.getId(),
              email,
              "LOGIN_FAILED",
              "AUTH",
              null,
              "Invalid email or password",
              httpRequest);
        } else {
          // User not found - log with email only
          auditLogService.logAction(
              null,
              email,
              "LOGIN_FAILED",
              "AUTH",
              null,
              "User not found",
              httpRequest);
        }
      } catch (Exception ex) {
        // Don't block if audit logging fails
      }
      throw new UnauthorizedException("Invalid email or password");
    }

    // 2. Load user
    user = userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found"));

    if (!user.getActive()) {
      // Audit log: Login failure - account disabled
      try {
        auditLogService.logAction(
            user.getId(),
            email,
            "LOGIN_FAILED",
            "AUTH",
            null,
            "User account is disabled",
            httpRequest);
      } catch (Exception e) {
        // Don't block if audit logging fails
      }
      throw new UnauthorizedException("User account is disabled");
    }

    // 2.5. Check if account registered with SSO (Google)
    if (user.getProvider() == LoginProvider.GOOGLE) {
      throw new UnauthorizedException("This account was registered with Google. Please login using Google Sign-In.");
    }

    // 3. Generate tokens với roles từ User entity
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    // 4. Lấy device & IP
    String deviceInfo = httpRequest.getHeader("User-Agent");
    String ipAddress = extractClientIp(httpRequest);

    // 5. Lưu refresh token (HASH)
    try {
      java.util.Date exp = jwtService.extractExpiration(refreshToken);
      LocalDateTime expiresAt = exp.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();

      String tokenHash = sha256Hex(refreshToken);

      RefreshToken rt = RefreshToken.builder()
          .tokenHash(tokenHash)
          .user(user)
          .expiresAt(expiresAt)
          .createdAt(LocalDateTime.now())
          .deviceInfo(deviceInfo)
          .ipAddress(ipAddress)
          .revoked(false)
          .build();

      refreshTokenRepository.save(rt);
    } catch (Exception e) {
      // Không block login nếu lỗi lưu token
      // (có thể log warn)
    }

    // 6. Audit log: Login success
    try {
      auditLogService.logAction(
          user.getId(),
          user.getEmail(),
          "LOGIN_SUCCESS",
          "AUTH",
          null,
          "User logged in successfully",
          httpRequest);
    } catch (Exception e) {
      // Don't block login if audit logging fails
    }

    // 7. Response
    Set<String> roles = user.getRoles()
        .stream()
        .map(r -> r.getName().name())
        .collect(Collectors.toSet());

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .roles(roles)
        .emailVerified(user.getEmailVerified())
        .build();
  }

  /**
   * Tạo hoặc cập nhật user từ OAuth2 provider (Google, Microsoft, etc.)
   *
   * <p>
   * Method này được gọi khi user đăng nhập lần đầu qua OAuth2.
   * Nếu user đã tồn tại, sẽ cập nhật thông tin (nếu cần).
   * Nếu user chưa tồn tại, sẽ tạo user mới với:
   * - emailVerified = true (vì OAuth2 provider đã verify email)
   * - role AUTHOR (mặc định)
   * - password được generate random (không thể login bằng password)
   *
   * @param email    Email từ OAuth2 provider
   * @param fullName Tên đầy đủ từ OAuth2 provider
   * @param provider Tên provider (google, azure, etc.)
   * @return User đã được tạo hoặc cập nhật
   */
  @Transactional
  public User createOrUpdateOAuth2User(String email, String fullName, String provider) {
    // Tìm user hiện có
    User user = userRepository.findByEmail(email).orElse(null);

    // Split fullName thành firstName và lastName
    String firstName;
    String lastName;
    if (fullName != null && !fullName.trim().isEmpty()) {
      String[] nameParts = fullName.trim().split("\\s+", 2);
      firstName = nameParts[0];
      lastName = nameParts.length > 1 ? nameParts[1] : "";
    } else {
      // Nếu không có fullName, sử dụng email prefix làm firstName
      firstName = email.split("@")[0];
      lastName = "";
    }

    if (user == null) {
      // Tạo user mới
      // Generate random password (user không thể login bằng password, chỉ OAuth2)
      String randomPassword = generateRandomPassword();

      // Map provider string to LoginProvider enum
      LoginProvider loginProvider = LoginProvider.LOCAL;
      if ("google".equalsIgnoreCase(provider)) {
        loginProvider = LoginProvider.GOOGLE;
      }

      user = User.builder()
          .email(email)
          .password(passwordEncoder.encode(randomPassword))
          .firstName(firstName)
          .lastName(lastName)
          .emailVerified(true) // OAuth2 providers đã verify email
          .active(true)
          .provider(loginProvider)
          .build();

      // Get or create AUTHOR role
      Role authorRole = roleRepository
          .findByName(RoleName.AUTHOR)
          .orElseGet(
              () -> {
                Role newRole = Role.builder().name(RoleName.AUTHOR).description("Role: AUTHOR").build();
                @SuppressWarnings("null")
                Role savedRole = roleRepository.save(newRole);
                return savedRole;
              });
      user.getRoles().add(authorRole);

      user = userRepository.save(user);
    } else {
      // Cập nhật user hiện có (nếu cần)
      // Chỉ cập nhật nếu thông tin mới hơn
      boolean updated = false;
      if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
        user.setFirstName(firstName);
        updated = true;
      }
      if (user.getLastName() == null || user.getLastName().isEmpty()) {
        user.setLastName(lastName);
        updated = true;
      }
      // Đảm bảo emailVerified = true cho OAuth2 users
      if (!user.getEmailVerified()) {
        user.setEmailVerified(true);
        updated = true;
      }
      // Đảm bảo user active
      if (!user.getActive()) {
        user.setActive(true);
        updated = true;
      }

      if (updated) {
        user = userRepository.save(user);
      }
    }

    return user;
  }

  /**
   * Generate random password cho OAuth2 users
   * OAuth2 users không thể login bằng password, chỉ có thể login qua OAuth2
   */
  private String generateRandomPassword() {
    // Generate random password (32 characters)
    // OAuth2 users không cần password để login, nhưng User entity yêu cầu password
    // không null
    java.util.UUID uuid = java.util.UUID.randomUUID();
    return uuid.toString().replace("-", "") + uuid.toString().replace("-", "");
  }

  private static String sha256Hex(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }

  @Transactional
  public LoginResponse loginWithGoogle(String idToken) throws Exception {

    GoogleUserInfo info = googleTokenService.verifyIdToken(idToken);

    String email = info.email();
    String fullName = info.name(); // vd: "Nguyen Van A"
    String googleId = info.sub();

    String firstName = fullName;
    String lastName = "";

    if (fullName != null && fullName.contains(" ")) {
      int idx = fullName.lastIndexOf(" ");
      firstName = fullName.substring(0, idx);
      lastName = fullName.substring(idx + 1);
    }

    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      // 🔹 TẠO USER MỚI
      user = User.builder()
          .email(email)
          .firstName(firstName)
          .lastName(lastName)
          .password(passwordEncoder.encode("GOOGLE_SSO_" + System.currentTimeMillis()))
          .provider(LoginProvider.GOOGLE)
          .providerId(googleId)
          .emailVerified(true) // Google email đã verify
          .active(true)
          .build();

      // 🔹 GÁN ROLE MẶC ĐỊNH: AUTHOR
      Role authorRole = roleRepository.findByName(RoleName.AUTHOR)
          .orElseThrow(() -> new RuntimeException("ROLE AUTHOR not found"));

      user.getRoles().add(authorRole);

      user = userRepository.save(user);
    } else {
      // 🔹 USER CŨ - Cập nhật thông tin
      if (user.getProvider() == LoginProvider.LOCAL) {
        // User đăng ký LOCAL, chuyển sang GOOGLE
        user.setProvider(LoginProvider.GOOGLE);
        user.setProviderId(googleId);
        user.setEmailVerified(true);

        if (user.getPassword() == null) {
          user.setPassword(
              passwordEncoder.encode("GOOGLE_SSO_" + System.currentTimeMillis()));
        }

        user = userRepository.save(user);
      } else if (user.getProvider() == LoginProvider.GOOGLE) {
        // User đã login với GOOGLE trước đó - đảm bảo password không null
        if (user.getPassword() == null) {
          user.setPassword(
              passwordEncoder.encode("GOOGLE_SSO_" + System.currentTimeMillis()));
          user = userRepository.save(user);
        }
        // Cập nhật providerId nếu cần
        if (user.getProviderId() == null || !user.getProviderId().equals(googleId)) {
          user.setProviderId(googleId);
          user = userRepository.save(user);
        }
      }
    }

    // 🔹 SINH JWT TOKENS với roles từ User entity
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);

    // 🔹 LƯU REFRESH TOKEN VÀO DATABASE
    try {
      java.util.Date exp = jwtService.extractExpiration(refreshToken);
      if (exp == null) {
        throw new BusinessException("Failed to extract refresh token expiration", "TOKEN_INVALID");
      }

      LocalDateTime expiresAt = exp.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();

      String tokenHash = sha256Hex(refreshToken);

      RefreshToken rt = RefreshToken.builder()
          .tokenHash(tokenHash)
          .user(user)
          .expiresAt(expiresAt)
          .createdAt(LocalDateTime.now())
          .deviceInfo("Google Sign-In")
          .ipAddress("")
          .revoked(false)
          .build();

      refreshTokenRepository.save(rt);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException("Failed to save refresh token: " + e.getMessage(), "TOKEN_SAVE_FAILED");
    }

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .emailVerified(user.getEmailVerified())
        .roles(
            user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet()))
        .build();
  }

  /**
   * Gửi email yêu cầu khôi phục mật khẩu.
   * Tạo một token khôi phục và gửi qua email cho người dùng.
   */
  @Transactional
  public void forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new NotFoundException("Email not found"));

    // Delete existing token if any
    passwordResetTokenRepository.deleteByUser(user);

    // Generate token
    String token = java.util.UUID.randomUUID().toString();
    PasswordResetToken resetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiryDate(LocalDateTime.now().plusHours(24))
        .build();

    passwordResetTokenRepository.save(resetToken);

    // Send email
    String resetUrl = "http://localhost:3000/reset-password?token=" + token;
    java.util.Map<String, Object> model = new java.util.HashMap<>();
    model.put("name", user.getFullName());
    model.put("resetUrl", resetUrl);

    try {
      emailService.sendEmail(user.getEmail(), "Password Reset Request - UTH-ConfMS", "forgot-password", model);
    } catch (Exception e) {
      log.error("Failed to send password reset email", e);
      // Fallback: send simple email if template fails
      emailService.sendSimpleEmail(user.getEmail(), "Password Reset Request",
          "Click the link to reset your password: " + resetUrl);
    }

    // Audit log
    auditLogService.logAction(user.getId(), user.getEmail(), "FORGOT_PASSWORD_REQUEST", "AUTH", null,
        "Forgot password request sent", httpRequest);
  }

  /**
   * Khôi phục mật khẩu bằng token.
   * Kiểm tra tính hợp lệ và thời hạn của token trước khi đặt lại mật khẩu.
   */
  @Transactional
  public void resetPassword(ResetPasswordRequest request, HttpServletRequest httpRequest) {
    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
        .orElseThrow(() -> new BusinessException("Invalid or expired token", "INVALID_TOKEN"));

    if (resetToken.isExpired()) {
      passwordResetTokenRepository.delete(resetToken);
      throw new BusinessException("Token has expired", "TOKEN_EXPIRED");
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // Delete token after use
    passwordResetTokenRepository.delete(resetToken);

    // Audit log
    auditLogService.logAction(user.getId(), user.getEmail(), "PASSWORD_RESET_SUCCESS", "AUTH", null,
        "Password reset successful via token", httpRequest);
  }

}
