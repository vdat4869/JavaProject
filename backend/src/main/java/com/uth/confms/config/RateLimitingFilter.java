package com.uth.confms.config;

import com.uth.confms.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.Timer;

/**
 * Filter để áp dụng rate limiting cho các request
 *
 * <p>Rate limiting được áp dụng dựa trên IP address.
 * Hỗ trợ cả Redis (distributed) và in-memory (local) mode.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "app.rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingFilter implements Filter {

  @Autowired(required = false)
  private ProxyManager<byte[]> proxyManager;

  @Value("${app.rate-limiting.auth.max-requests:5}")
  private int authMaxRequests;

  @Value("${app.rate-limiting.auth.window-seconds:60}")
  private int authWindowSeconds;

  @Value("${app.rate-limiting.api.max-requests:100}")
  private int apiMaxRequests;

  @Value("${app.rate-limiting.api.window-seconds:60}")
  private int apiWindowSeconds;

  @Autowired(required = false)
  private RateLimitingMetrics metrics;

  // In-memory buckets khi không có Redis
  private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void doFilter(
      jakarta.servlet.ServletRequest request,
      jakarta.servlet.ServletResponse response,
      FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    Timer.Sample timerSample = metrics != null ? metrics.startTimer() : null;
    String path = httpRequest.getRequestURI();
    
    try {
      String key = resolveKey(httpRequest);

      // Cấu hình rate limit khác nhau cho auth endpoints
      int maxRequests;
      int windowSeconds;
      if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
        maxRequests = authMaxRequests;
        windowSeconds = authWindowSeconds;
      } else {
        maxRequests = apiMaxRequests;
        windowSeconds = apiWindowSeconds;
      }

      Bucket bucket = resolveBucket(key, maxRequests, windowSeconds);

      if (bucket.tryConsume(1)) {
        if (metrics != null) {
          metrics.recordRateLimitAllowed(httpRequest);
        }
        chain.doFilter(request, response);
      } else {
        if (metrics != null) {
          metrics.recordRateLimitExceeded(httpRequest);
        }
        handleRateLimitExceeded(httpResponse);
      }
    } finally {
      if (timerSample != null && metrics != null) {
        metrics.stopTimer(timerSample, path);
      }
    }
  }

  private String resolveKey(HttpServletRequest request) {
    // Sử dụng IP address làm key
    String ipAddress = getClientIpAddress(request);
    String path = request.getRequestURI();
    return String.format("rate-limit:%s:%s", ipAddress, path);
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }
    return request.getRemoteAddr();
  }

  private Bucket resolveBucket(String key, int maxRequests, int windowSeconds) {
    // Sử dụng Redis nếu có, ngược lại dùng in-memory
    if (proxyManager != null) {
      return resolveDistributedBucket(key, maxRequests, windowSeconds);
    } else {
      return resolveLocalBucket(key, maxRequests, windowSeconds);
    }
  }

  private Bucket resolveDistributedBucket(String key, int maxRequests, int windowSeconds) {
    Supplier<BucketConfiguration> configSupplier = () -> {
      Bandwidth limit = Bandwidth.builder()
          .capacity(maxRequests)
          .refillIntervally(maxRequests, Duration.ofSeconds(windowSeconds))
          .build();
      return BucketConfiguration.builder()
          .addLimit(limit)
          .build();
    };
    byte[] keyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    return proxyManager.builder()
        .build(keyBytes, configSupplier);
  }

  private Bucket resolveLocalBucket(String key, int maxRequests, int windowSeconds) {
    return localBuckets.computeIfAbsent(key, k -> {
      Bandwidth limit = Bandwidth.builder()
          .capacity(maxRequests)
          .refillIntervally(maxRequests, Duration.ofSeconds(windowSeconds))
          .build();
      return Bucket.builder()
          .addLimit(limit)
          .build();
    });
  }

  private void handleRateLimitExceeded(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> apiResponse = ApiResponse.error(
        "Too many requests. Please try again later.");

    objectMapper.writeValue(response.getWriter(), apiResponse);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Không cần khởi tạo gì
  }

  @Override
  public void destroy() {
    localBuckets.clear();
  }
}
