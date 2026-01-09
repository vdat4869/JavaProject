package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.LoginRequest;
import com.uth.confms.auth.dto.LoginResponse;
import com.uth.confms.auth.dto.SignupRequest;
import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.entity.Role.RoleName;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.RoleRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import com.uth.confms.email.service.EmailVerificationService;
import java.util.Set;
import java.util.stream.Collectors;
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

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      EmailVerificationService emailVerificationService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.emailVerificationService = emailVerificationService;
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
  public LoginResponse signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException("Email already exists", "EMAIL_EXISTS");
    }

    User user =
        User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .affiliation(request.getAffiliation())
            .phone(request.getPhone())
            .emailVerified(true)
            .active(true)
            .build();

    // Get or create AUTHOR role
    Role authorRole =
        roleRepository
            .findByName(RoleName.AUTHOR)
            .orElseGet(
                () -> {
                  // Auto-create AUTHOR role if not exists
                  Role newRole =
                      Role.builder()
                          .name(RoleName.AUTHOR)
                          .description("Role: AUTHOR")
                          .build();
                  @SuppressWarnings("null")
                  Role savedRole = roleRepository.save(newRole);
                  return savedRole;
                });
    user.getRoles().add(authorRole);

    user = userRepository.save(user);

    // TODO: Email verification disabled - auto-verify on signup
    // emailVerificationService.sendVerificationEmail(user);

    // Generate tokens immediately since email is auto-verified
    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    Set<String> roles =
        user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .userId(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .roles(roles)
        .emailVerified(true)
        .build();
  }

  /**
   * Đăng nhập người dùng vào hệ thống
   *
   * @param request Thông tin đăng nhập (email, password)
   * @return LoginResponse chứa access token, refresh token và thông tin user
   * @throws UnauthorizedException Nếu email/password sai hoặc account bị disable
   * @throws NotFoundException Nếu không tìm thấy user
   */
  public LoginResponse login(LoginRequest request) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException("Invalid email or password");
    }

    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException("User not found"));

    if (!user.getActive()) {
      throw new UnauthorizedException("User account is disabled");
    }

    // TODO: Email verification required before login (temporarily disabled)
    // if (!user.getEmailVerified()) {
    //   throw new UnauthorizedException("Email verification required. Please check your email.");
    // }

    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    Set<String> roles =
        user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());

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
}
