package com.uth.confms.config;

import com.uth.confms.auth.service.AuthService;
import com.uth.confms.auth.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cấu hình OAuth2/SSO Support với Google
 *
 * <p>
 * Hỗ trợ đăng nhập qua Google OAuth2
 *
 * <p>
 * Để bật OAuth2/SSO, cấu hình trong application.yaml:
 * 
 * <pre>
 * app:
 *   oauth2:
 *     enabled: true
 *     providers:
 *       google:
 *         client-id: ${GOOGLE_CLIENT_ID:}
 *         client-secret: ${GOOGLE_CLIENT_SECRET:}
 * </pre>
 *
 * <p>
 * Và cấu hình Spring Security OAuth2:
 * 
 * <pre>
 * spring:
 *   security:
 *     oauth2:
 *       client:
 *         registration:
 *           google:
 *             client-id: ${GOOGLE_CLIENT_ID:}
 *             client-secret: ${GOOGLE_CLIENT_SECRET:}
 *             scope: openid,profile,email
 *             redirect-uri: http://localhost:8080/login/oauth2/code/google
 * </pre>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(name = "app.oauth2.enabled", havingValue = "true", matchIfMissing = false)
public class OAuth2Config {

  private static final Logger log = LoggerFactory.getLogger(OAuth2Config.class);

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final AuthService authService;

  public OAuth2Config(
      @org.springframework.context.annotation.Lazy AuthService authService,
      JwtService jwtService,
      UserDetailsService userDetailsService) {
    this.authService = authService;
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Custom OAuth2 User Service để tích hợp với hệ thống JWT hiện có
   *
   * <p>
   * Service này sẽ:
   * 1. Load user info từ OAuth2 provider (Google)
   * 2. Tạo hoặc cập nhật user trong database
   * 3. Trả về OidcUser để Spring Security xử lý
   */
  @Bean
  // Xử lý user info trả về từ Google
  public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
    OidcUserService delegate = new OidcUserService();
    return (userRequest) -> {
      OidcUser oidcUser = delegate.loadUser(userRequest);

      // Tạo hoặc cập nhật user trong database
      String email = oidcUser.getEmail();
      String fullName = oidcUser.getFullName();
      String provider = userRequest.getClientRegistration().getRegistrationId();

      if (email != null && !email.trim().isEmpty()) {
        try {
          authService.createOrUpdateOAuth2User(email, fullName, provider);
        } catch (Exception e) {
          // Log error nhưng không throw để không block OAuth2 flow
          // User sẽ được redirect về trang đăng ký nếu có lỗi
          log.warn("Failed to create/update OAuth2 user: {}", e.getMessage(), e);
        }
      }

      return oidcUser;
    };
  }

  /**
   * Authentication Success Handler để tạo JWT token sau khi OAuth2 login thành
   * công
   */
  @Bean
  // Handler xử lý khi login thành công (Tạo JWT và redirect về frontend)
  public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
    return new SimpleUrlAuthenticationSuccessHandler() {
      @Override
      public void onAuthenticationSuccess(
          HttpServletRequest request,
          HttpServletResponse response,
          Authentication authentication)
          throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
          String email = oidcUser.getEmail();

          try {
            // Load user details và tạo JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Redirect về frontend với token
            String redirectUrl = String.format(
                "%s/auth/sso/callback?token=%s&refreshToken=%s",
                frontendUrl,
                accessToken,
                refreshToken);

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
          } catch (Exception e) {
            // Nếu user chưa tồn tại, redirect về trang đăng ký
            String redirectUrl = String.format(
                "%s/auth/oauth2/register?email=%s&name=%s&provider=%s",
                frontendUrl,
                email,
                oidcUser.getFullName(),
                oidcUser.getIssuer());

            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
          }
        } else {
          super.onAuthenticationSuccess(request, response, authentication);
        }
      }
    };
  }
}
