package com.uth.confms.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Metrics cho Rate Limiting
 *
 * <p>Thu thập các metrics về:
 * <ul>
 *   <li>Số lượng request bị rate limit
 *   <li>Thời gian xử lý rate limiting
 *   <li>Phân loại theo endpoint và IP
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Component
public class RateLimitingMetrics {

  private final MeterRegistry meterRegistry;

  public RateLimitingMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  /**
   * Ghi lại metric khi request bị từ chối do rate limit
   */
  public void recordRateLimitExceeded(HttpServletRequest request) {
    Counter.builder("rate.limit.exceeded")
        .description("Số lượng request bị từ chối do vượt quá rate limit")
        .tag("component", "rate-limiting")
        .tag("path", getPath(request))
        .tag("method", request.getMethod())
        .tag("ip", getClientIp(request))
        .register(meterRegistry)
        .increment();
  }

  /**
   * Ghi lại metric khi request được phép qua
   */
  public void recordRateLimitAllowed(HttpServletRequest request) {
    Counter.builder("rate.limit.allowed")
        .description("Số lượng request được phép qua rate limiting")
        .tag("component", "rate-limiting")
        .tag("path", getPath(request))
        .tag("method", request.getMethod())
        .register(meterRegistry)
        .increment();
  }

  /**
   * Tạo timer để đo thời gian xử lý rate limiting
   */
  public Timer.Sample startTimer() {
    return Timer.start(meterRegistry);
  }

  /**
   * Dừng timer và ghi lại metric
   */
  public void stopTimer(Timer.Sample sample, String path) {
    if (sample != null) {
      Timer timer = Timer.builder("rate.limit.processing.time")
          .description("Thời gian xử lý rate limiting")
          .tag("component", "rate-limiting")
          .tag("path", path)
          .register(meterRegistry);
      sample.stop(timer);
    }
  }

  private String getPath(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Normalize path để tránh quá nhiều tags
    if (path.startsWith("/api/auth/")) {
      return "/api/auth/**";
    }
    if (path.startsWith("/api/")) {
      return "/api/**";
    }
    return path;
  }

  private String getClientIp(HttpServletRequest request) {
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
}
