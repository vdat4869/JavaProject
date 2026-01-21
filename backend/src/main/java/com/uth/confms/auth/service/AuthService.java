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
import com.uth.confms.auth.security.JwtUtil;
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
import org.springframework.security.core.userdetails.UserDetails;
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
  private final UserDetailsService userDetailsService;
  @SuppressWarnings("unused")
  private final EmailVerificationService emailVerificationService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final GoogleTokenService googleTokenService;
  private final JwtUtil jwtUtil;

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      EmailVerificationService emailVerificationService,
      RefreshTokenRepository refreshTokenRepository,
      GoogleTokenService googleTokenService,
      JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.emailVerificationService = emailVerificationService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.googleTokenService = googleTokenService;
    this.jwtUtil = jwtUtil;
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
  public void changePassword(Long userId, ChangePasswordRequest request) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new UnauthorizedException("Current password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  /**
   * Logout - currently a no-op because token invalidation is handled on the
   * client or via token blacklist if implemented.
   */
  public void logout(String refreshToken) {
    String tokenHash = sha256Hex(refreshToken);
    refreshTokenRepository.revokeByTokenHash(tokenHash);
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

    // 1. Authenticate
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getEmail(),
              request.getPassword()));
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException("Invalid email or password");
    }

    // 2. Load user
    User user = userRepository
        .findByEmail(request.getEmail())
        .orElseThrow(() -> new NotFoundException("User not found"));

    if (!user.getActive()) {
      throw new UnauthorizedException("User account is disabled");
    }

    // 3. Generate tokens
    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

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

    // 6. Response
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
          .provider(LoginProvider.GOOGLE)
          .providerId(googleId)
          .emailVerified(true) // Google email đã verify
          .active(true)
          .build();

      // 🔹 GÁN ROLE MẶC ĐỊNH: AUTHOR
      Role authorRole = roleRepository.findByName(RoleName.AUTHOR)
          .orElseThrow(() -> new RuntimeException("ROLE AUTHOR not found"));

      user.getRoles().add(authorRole);

      userRepository.save(user);
    } else {
      // 🔹 USER CŨ (đăng ký LOCAL)
      if (user.getProvider() == LoginProvider.LOCAL) {
        user.setProvider(LoginProvider.GOOGLE);
        user.setProviderId(googleId);
        user.setEmailVerified(true);
      }
    }

    // 🔹 SINH JWT CHUNG
    String token = jwtUtil.generateToken(user);

    return LoginResponse.builder()
        .accessToken(token)
        .tokenType("Bearer")
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .emailVerified(user.getEmailVerified())
        .roles(
            user.getRoles().stream()
                .map(r -> r.getName().name()) // RoleName -> String
                .collect(Collectors.toSet()))
        .build();
  }

}
