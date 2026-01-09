package com.uth.confms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoggingConfig implements WebMvcConfigurer {

  // Logging interceptor can be added here if needed
  // For now, using Spring's built-in logging

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    // Add custom interceptors here if needed
  }
}
