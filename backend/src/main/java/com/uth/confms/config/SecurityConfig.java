package com.uth.confms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
/**
 * Cấu hình bảo mật chính của ứng dụng (Spring Security).
 *
 * <p>
 * Class này chịu trách nhiệm:
 * <ul>
 * <li>Cấu hình Security Filter Chain (CSRF, CORS, authorize requests)</li>
 * <li>Tích hợp JWT Authentication Filter</li>
 * <li>Cấu hình OAuth2 Login (Google)</li>
 * <li>Quản lý AuthenticationManager và PasswordEncoder</li>
 * <li>Xử lý exception liên quan đến bảo mật (AuthenticationEntryPoint)</li>
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserDetailsService userDetailsService;
  private final CorsConfigurationSource corsConfigurationSource;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Autowired(required = false)
  private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

  @Autowired(required = false)
  private AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthFilter,
      UserDetailsService userDetailsService,
      CorsConfigurationSource corsConfigurationSource,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.userDetailsService = userDetailsService;
    this.corsConfigurationSource = corsConfigurationSource;
    this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
  }

  @Bean
  // Cấu hình filter chain bảo mật
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(
                "/api/auth/**",
                "/api/conferences/public",
                "/api/organizations",
                "/oauth2/**",
                "/login/oauth2/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/api-docs/**",
                "/v3/api-docs/**",
                "/favicon.ico",
                "/error")
                .permitAll()
                .anyRequest()
                .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider());

    // Tích hợp OAuth2 nếu được bật
    if (oidcUserService != null && oauth2AuthenticationSuccessHandler != null) {
      http.oauth2Login(oauth2 -> oauth2
          .userInfoEndpoint(userInfo -> userInfo
              .oidcUserService(oidcUserService))
          .successHandler(oauth2AuthenticationSuccessHandler));
    }

    http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    // Xử lý exception khi chưa authen (trả về 401 thay vì redirect)
    http.exceptionHandling(exception -> exception
        .authenticationEntryPoint(customAuthenticationEntryPoint));

    return http.build();
  }

  @Bean
  // Provider xác thực user (DAO pattern)
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  // Bean mã hóa mật khẩu (BCrypt)
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
