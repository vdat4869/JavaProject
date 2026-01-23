package com.uth.confms.config;

import com.uth.confms.common.interceptor.RequireRoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration cho Web MVC
 *
 * <p>Đăng ký các interceptors:
 * <ul>
 *   <li>RequireRoleInterceptor - Xử lý @RequireRole annotation
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final RequireRoleInterceptor requireRoleInterceptor;

  public WebMvcConfig(RequireRoleInterceptor requireRoleInterceptor) {
    this.requireRoleInterceptor = requireRoleInterceptor;
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    // Register RequireRoleInterceptor với order cao để chạy sau authentication
    // Order 1: Chạy sau authentication filters nhưng trước business logic
    registry.addInterceptor(requireRoleInterceptor)
        .addPathPatterns("/api/**")
        .order(1);
  }
}
