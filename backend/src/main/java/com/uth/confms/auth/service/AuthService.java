package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.ChangePasswordRequest;
import com.uth.confms.auth.dto.GoogleUserInfo;
import com.uth.confms.auth.dto.LoginRequest;
import com.uth.confms.auth.dto.LoginResponse;
import com.uth.confms.auth.dto.RegisterRequest;
import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.entity.RefreshToken;
import com.uth.confms.auth.enums.RoleName;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.RoleRepository;
import com.uth.confms.auth.repository.RefreshTokenRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.email.service.EmailVerificationService;
import com.uth.confms.auth.enums.LoginProvider;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.nio.charset.StandardCharsets;
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
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  @SuppressWarnings("unused")
  private final UserDetailsService userDetailsService;
  @SuppressWarnings("unused")
  private final EmailVerificationService emailVerificationService;
  private final RefreshTokenRepository refreshTokenRepository;
<<<<<<< HEAD
  private final AuditLogService auditLogService;
=======
  private final GoogleTokenService googleTokenService;
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      EmailVerificationService emailVerificationService,
      RefreshTokenRepository refreshTokenRepository,
<<<<<<< HEAD
      AuditLogService auditLogService) {
=======
      GoogleTokenService googleTokenService) {
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.emailVerificationService = emailVerificationService;
    this.refreshTokenRepository = refreshTokenRepository;
<<<<<<< HEAD
    this.auditLogService = auditLogService;
=======
    this.googleTokenService = googleTokenService;
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af
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

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .affiliation(request.getAffiliation())
        .phone(request.getPhone())
        .emailVerified(false)
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

    // Send verification email (may log/warn if email sending fails)
    try {
      emailVerificationService.sendVerificationEmail(user);
    } catch (Exception e) {
      // swallow to avoid blocking registration
    }

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

    // Return minimal response without tokens. Frontend expects to navigate to
    // verify-email.
    Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());

    return LoginResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .roles(roles)
        .emailVerified(user.getEmailVerified())
        .tokenType("Bearer")
        .build();
  }

  /**
   * Verify email using token and mark user as verified
   */
  @Transactional
  public void verifyEmail(String token) {
    emailVerificationService.verifyEmail(token);
  }

  /**
   * Resend verification token to user email
   */
  @Transactional
  public void resendVerification(String email) {
    emailVerificationService.resendVerificationToken(email);
  }

  /**
   * Change password for user
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
   * Logout - currently a no-op because token invalidation is handled on the
   * client or via token blacklist if implemented.
   */
  public void logout(String refreshToken, HttpServletRequest httpRequest) {
    String tokenHash = sha256Hex(refreshToken);
<<<<<<< HEAD
    refreshTokenRepository.revokeByTokenHash(tokenHash);

    // Try to get user from refresh token for audit logging
    try {
      // Find refresh token to get user
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
      // Don't block logout if audit logging fails
    }
=======
    refreshTokenRepository.revokeByTokenHash(tokenHash, LocalDateTime.now());
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af
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
   * <p>Method này được gọi khi user đăng nhập lần đầu qua OAuth2.
   * Nếu user đã tồn tại, sẽ cập nhật thông tin (nếu cần).
   * Nếu user chưa tồn tại, sẽ tạo user mới với:
   * - emailVerified = true (vì OAuth2 provider đã verify email)
   * - role AUTHOR (mặc định)
   * - password được generate random (không thể login bằng password)
   *
   * @param email Email từ OAuth2 provider
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

      user = User.builder()
          .email(email)
          .password(passwordEncoder.encode(randomPassword))
          .firstName(firstName)
          .lastName(lastName)
          .emailVerified(true) // OAuth2 providers đã verify email
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
    // OAuth2 users không cần password để login, nhưng User entity yêu cầu password không null
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

}
