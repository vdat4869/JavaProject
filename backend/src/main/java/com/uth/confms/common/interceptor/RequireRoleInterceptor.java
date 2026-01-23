package com.uth.confms.common.interceptor;

import com.uth.confms.common.annotations.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor để xử lý @RequireRole annotation
 *
 * <p>Interceptor này check roles từ SecurityContext khi method hoặc class
 * có annotation @RequireRole. Nếu user không có required roles, sẽ throw
 * AccessDeniedException.
 *
 * <p>Usage:
 * <pre>
 * @RequireRole({"ADMIN", "CHAIR"})
 * public ResponseEntity&lt;ApiResponse&lt;T&gt;&gt; someMethod() {
 *   // Method chỉ accessible bởi ADMIN hoặc CHAIR
 * }
 * </pre>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Component
public class RequireRoleInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(RequireRoleInterceptor.class);

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler) throws Exception {

    // Chỉ xử lý HandlerMethod (controller methods)
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    HandlerMethod handlerMethod = (HandlerMethod) handler;
    Method method = handlerMethod.getMethod();
    Class<?> controllerClass = handlerMethod.getBeanType();

    // Check @RequireRole trên method trước, nếu không có thì check trên class
    RequireRole requireRole = AnnotationUtils.findAnnotation(method, RequireRole.class);
    if (requireRole == null) {
      requireRole = AnnotationUtils.findAnnotation(controllerClass, RequireRole.class);
    }

    // Nếu không có @RequireRole, cho phép tiếp tục
    if (requireRole == null) {
      return true;
    }

    // Lấy authentication từ SecurityContext
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // Nếu chưa authenticate, throw AccessDeniedException
    if (authentication == null || !authentication.isAuthenticated()) {
      log.warn("Unauthenticated access attempt to @RequireRole protected endpoint: {}", request.getRequestURI());
      throw new AccessDeniedException("Authentication required");
    }

    // Lấy required roles từ annotation
    String[] requiredRoles = requireRole.value();
    if (requiredRoles == null || requiredRoles.length == 0) {
      // Nếu không có roles nào được specify, cho phép tiếp tục
      return true;
    }

    // Check nếu user có ít nhất 1 trong các required roles
    boolean hasRequiredRole = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(authority -> {
          // Spring Security thường prefix roles với "ROLE_"
          String role = authority.startsWith("ROLE_") 
              ? authority.substring(5) 
              : authority;
          
          for (String requiredRole : requiredRoles) {
            if (role.equalsIgnoreCase(requiredRole)) {
              return true;
            }
          }
          return false;
        });

    if (!hasRequiredRole) {
      log.warn(
          "Access denied for user '{}' to endpoint '{}'. Required roles: {}, User roles: {}",
          authentication.getName(),
          request.getRequestURI(),
          String.join(", ", requiredRoles),
          authentication.getAuthorities());
      throw new AccessDeniedException(
          "Access denied. Required roles: " + String.join(", ", requiredRoles));
    }

    return true;
  }
}
