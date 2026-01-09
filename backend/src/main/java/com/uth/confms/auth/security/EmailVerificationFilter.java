package com.uth.confms.auth.security;

// import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.lang.NonNull;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class EmailVerificationFilter extends OncePerRequestFilter {
  @SuppressWarnings("unused")
  private final UserRepository userRepository;

  private static final Set<String> PUBLIC_ENDPOINTS =
      Set.of(
          "/api/auth/login",
          "/api/auth/register",
          "/api/auth/email-verification",
          "/api/conferences/public",
          "/swagger-ui",
          "/swagger-ui.html",
          "/api-docs",
          "/v3/api-docs",
          "/favicon.ico");

  public EmailVerificationFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    return isPublicEndpoint(path);
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    // Skip check for public endpoints
    if (isPublicEndpoint(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    // TODO: Email verification check (temporarily disabled)
    // Email verification filter is completely disabled
    // All requests are allowed through without checking email verification status
    // 
    // Previous code (commented out):
    // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    // if (authentication != null && authentication.isAuthenticated()) {
    //   String email = authentication.getName();
    //   User user = userRepository.findByEmail(email).orElse(null);
    //
    //   if (user != null && !user.getEmailVerified()) {
    //     // Check if this is a workflow endpoint that requires email verification
    //     if (requiresEmailVerification(path)) {
    //       response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    //       response.setContentType("application/json");
    //       response.getWriter().write("{\"error\":\"Email verification required\"}");
    //       return;
    //     }
    //   }
    // }

    filterChain.doFilter(request, response);
  }

  private boolean isPublicEndpoint(String path) {
    // Check exact matches and path prefixes
    return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/api-docs/")
        || path.startsWith("/v3/api-docs");
  }

  @SuppressWarnings("unused")
  private boolean requiresEmailVerification(String path) {
    // Endpoints that require email verification (temporarily disabled)
    return path.startsWith("/api/submissions")
        || path.startsWith("/api/review")
        || path.startsWith("/api/assignment")
        || path.startsWith("/api/decision")
        || path.startsWith("/api/cameraready");
  }
}
