package com.uth.confms.auth.service;

import com.uth.confms.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service xử lý JWT tokens
 *
 * <p>
 * Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 * <li>Generate access token và refresh token
 * <li>Validate tokens
 * <li>Extract claims từ tokens
 * <li>Token expiration management
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
public class JwtService {
  private static final Logger log = LoggerFactory.getLogger(JwtService.class);

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token-validity}")
  private Long accessTokenValidity;

  @Value("${jwt.refresh-token-validity}")
  private Long refreshTokenValidity;

  private SecretKey getSigningKey() {
    if (secret == null || secret.trim().isEmpty()) {
      throw new IllegalStateException("JWT secret key is not configured");
    }
    // Ensure secret is at least 32 characters for HS256
    if (secret.length() < 32) {
      throw new IllegalStateException("JWT secret key must be at least 32 characters long");
    }
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String extractUsername(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    try {
      return extractClaim(token, Claims::getSubject);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage(), e);
    }
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extract roles từ JWT token
   * 
   * @param token JWT token
   * @return List of role names, hoặc empty list nếu không có roles
   */
  @SuppressWarnings("unchecked")
  public List<String> extractRoles(String token) {
    try {
      Claims claims = extractAllClaims(token);
      Object rolesObj = claims.get("roles");
      if (rolesObj instanceof List) {
        return (List<String>) rolesObj;
      }
      return List.of();
    } catch (Exception e) {
      log.warn("Failed to extract roles from token: {}", e.getMessage());
      return List.of();
    }
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("Token cannot be null or empty");
    }
    try {
      return Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (io.jsonwebtoken.security.SecurityException e) {
      log.warn("JWT signature validation failed: {}", e.getMessage());
      throw new IllegalArgumentException("JWT signature validation failed: " + e.getMessage(), e);
    } catch (io.jsonwebtoken.ExpiredJwtException e) {
      log.warn("JWT token has expired: {}", e.getMessage());
      throw new IllegalArgumentException("JWT token has expired: " + e.getMessage(), e);
    } catch (io.jsonwebtoken.MalformedJwtException e) {
      log.warn("Malformed JWT token: {}", e.getMessage());
      throw new IllegalArgumentException("Malformed JWT token: " + e.getMessage(), e);
    } catch (Exception e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage(), e);
    }
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateAccessToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = userDetails.getAuthorities().stream()
        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    claims.put("roles", roles);
    return createToken(claims, userDetails.getUsername(), accessTokenValidity);
  }

  /**
   * Generate access token với roles từ User entity
   * 
   * @param user User entity để lấy roles
   * @return JWT access token với roles trong claims
   */
  public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    // Không thêm prefix "ROLE_" vì đã config setDefaultRolePrefix("") trong
    // MethodSecurityConfig
    List<String> roles = user.getRoles().stream()
        .map(role -> role.getName().name())
        .collect(Collectors.toList());
    claims.put("roles", roles);
    return createToken(claims, user.getEmail(), accessTokenValidity);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = userDetails.getAuthorities().stream()
        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
    claims.put("roles", roles);
    return createToken(claims, userDetails.getUsername(), refreshTokenValidity);
  }

  /**
   * Generate refresh token với roles từ User entity
   * 
   * @param user User entity để lấy roles
   * @return JWT refresh token với roles trong claims
   */
  public String generateRefreshToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    List<String> roles = user.getRoles().stream()
        .map(role -> role.getName().name())
        .collect(Collectors.toList());
    claims.put("roles", roles);
    return createToken(claims, user.getEmail(), refreshTokenValidity);
  }

  private String createToken(Map<String, Object> claims, String subject, Long validity) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + validity))
        .signWith(getSigningKey())
        .compact();
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = extractUsername(token);
      boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
      if (!isValid) {
        log.warn("Token validation failed for user: {}", userDetails.getUsername());
      }
      return isValid;
    } catch (Exception e) {
      log.warn("Token validation error: {}", e.getMessage());
      return false;
    }
  }
}
