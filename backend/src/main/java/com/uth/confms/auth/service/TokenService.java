package com.uth.confms.auth.service;

import com.uth.confms.common.exception.UnauthorizedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Service xử lý refresh tokens
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Refresh access token từ refresh token
 *   <li>Validate refresh token
 *   <li>Token expiration checks
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
public class TokenService {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public TokenService(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  public String refreshAccessToken(String refreshToken) {
    try {
      String email = jwtService.extractUsername(refreshToken);
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      if (jwtService.validateToken(refreshToken, userDetails)) {
        return jwtService.generateAccessToken(userDetails);
      }

      throw new UnauthorizedException("Invalid refresh token");
    } catch (Exception e) {
      throw new UnauthorizedException("Invalid refresh token");
    }
  }
}
