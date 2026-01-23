package com.uth.confms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration để enable custom SpEL methods cho @PreAuthorize
 *
 * <p>Cho phép sử dụng các methods từ ConferenceAuthorizationService trong @PreAuthorize:
 * <pre>
 * @PreAuthorize("@conferenceAuthorizationService.isChairOfConferenceByEmail(authentication.name, #id)")
 * </pre>
 *
 * <p>Cho phép sử dụng hasPermission() trong @PreAuthorize:
 * <pre>
 * @PreAuthorize("hasPermission(#id, 'Conference', 'UPDATE')")
 * </pre>
 *
 * <p>Note: @EnableMethodSecurity được enable trong SecurityConfig, config này chỉ để customize expression handler.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {

  private final PermissionEvaluator permissionEvaluator;

  public MethodSecurityConfig(PermissionEvaluator permissionEvaluator) {
    this.permissionEvaluator = permissionEvaluator;
  }

  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
    DefaultMethodSecurityExpressionHandler expressionHandler =
        new DefaultMethodSecurityExpressionHandler();
    expressionHandler.setDefaultRolePrefix(""); // Remove "ROLE_" prefix
    expressionHandler.setPermissionEvaluator(permissionEvaluator); // Enable hasPermission()
    return expressionHandler;
  }
}
