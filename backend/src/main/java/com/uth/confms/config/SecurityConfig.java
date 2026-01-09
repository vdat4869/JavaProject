package com.uth.confms.config;

import com.uth.confms.auth.security.EmailVerificationFilter;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthFilter;
  private final EmailVerificationFilter emailVerificationFilter;
  private final UserDetailsService userDetailsService;
  private final CorsConfigurationSource corsConfigurationSource;

  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthFilter,
      EmailVerificationFilter emailVerificationFilter,
      UserDetailsService userDetailsService,
      CorsConfigurationSource corsConfigurationSource) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.emailVerificationFilter = emailVerificationFilter;
    this.userDetailsService = userDetailsService;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/auth/**",
                        "/api/conferences/public",
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
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(emailVerificationFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
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
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
