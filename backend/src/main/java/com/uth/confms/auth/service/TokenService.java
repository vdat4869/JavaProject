package com.uth.confms.auth.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.RefreshTokenRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import com.uth.confms.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Service xử lý refresh tokens
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Refresh access token từ refresh token
 * <li>Validate refresh token
 * <li>Token expiration checks
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
public class TokenService {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final RefreshTokenRepository refreshTokenRepository;
<<<<<<< HEAD
  private final AuditLogService auditLogService;
=======
  private final UserRepository userRepository;
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af

  public TokenService(JwtService jwtService, UserDetailsService userDetailsService,
<<<<<<< HEAD
      RefreshTokenRepository refreshTokenRepository, AuditLogService auditLogService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.auditLogService = auditLogService;
=======
      RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af
  }

  public String refreshAccessToken(String refreshToken, HttpServletRequest httpRequest) {
    try {
      // verify refresh token exists in DB and not expired (compare by hash)
      String tokenHash = sha256Hex(refreshToken);
      Optional<com.uth.confms.auth.entity.RefreshToken> rtOpt = refreshTokenRepository
          .findByTokenHashAndRevokedFalse(tokenHash);
      if (rtOpt.isEmpty()) {
        throw new UnauthorizedException("Invalid refresh token");
      }
      if (rtOpt.get().getExpiresAt() != null && rtOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
        // token expired in DB
        refreshTokenRepository.delete(rtOpt.get());
        throw new UnauthorizedException("Invalid refresh token");
      }

      String email = jwtService.extractUsername(refreshToken);
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      if (jwtService.validateToken(refreshToken, userDetails)) {
<<<<<<< HEAD
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        
        // Audit log: Token refresh
        try {
          if (rtOpt.isPresent() && rtOpt.get().getUser() != null) {
            var user = rtOpt.get().getUser();
            auditLogService.logAction(
                user.getId(),
                email,
                "TOKEN_REFRESHED",
                "AUTH",
                null,
                "Access token refreshed successfully",
                httpRequest);
          }
        } catch (Exception e) {
          // Don't block token refresh if audit logging fails
        }
        
        return newAccessToken;
=======
        // Load User entity để lấy roles và generate token với roles
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return jwtService.generateAccessToken(user);
>>>>>>> 8dc352787c60bcc2c30894e3d3dab6d5850520af
      }

      throw new UnauthorizedException("Invalid refresh token");
    } catch (Exception e) {
      throw new UnauthorizedException("Invalid refresh token");
    }
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
}
