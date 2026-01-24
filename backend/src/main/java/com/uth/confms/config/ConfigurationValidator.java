package com.uth.confms.config;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Validator để kiểm tra các biến môi trường và cấu hình bắt buộc khi ứng dụng khởi động
 *
 * <p>Validator này sẽ:
 * <ul>
 *   <li>Kiểm tra các biến môi trường bắt buộc
 *   <li>Validate format của các giá trị cấu hình
 *   <li>Fail fast nếu có cấu hình không hợp lệ
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Component
@Profile("!dev")
public class ConfigurationValidator {

  private static final Logger log = LoggerFactory.getLogger(ConfigurationValidator.class);

  private final Environment environment;

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  public ConfigurationValidator(Environment environment) {
    this.environment = environment;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void validateConfiguration() {
    log.info("Bắt đầu validate cấu hình...");
    List<String> errors = new ArrayList<>();

    // Validate database configuration
    validateDatabaseConfig(errors);

    // Validate JWT configuration
    validateJwtConfig(errors);

    // Validate production-specific configurations
    if (isProductionProfile()) {
      validateProductionConfig(errors);
    }

    // Validate mail configuration (nếu được bật)
    validateMailConfig(errors);

    // Validate Redis configuration (nếu rate limiting sử dụng Redis)
    if (isRateLimitingWithRedis()) {
      validateRedisConfig(errors);
    }

    if (!errors.isEmpty()) {
      log.error("=== CÁC LỖI CẤU HÌNH ===");
      errors.forEach(log::error);
      log.error("=== KẾT THÚC DANH SÁCH LỖI ===");
      throw new IllegalStateException(
          "Cấu hình không hợp lệ. Vui lòng kiểm tra các biến môi trường bắt buộc.\n"
              + String.join("\n", errors));
    }

    log.info("✅ Tất cả cấu hình đã được validate thành công");
  }

  private void validateDatabaseConfig(List<String> errors) {
    String dbUrl = environment.getProperty("spring.datasource.url");
    
    // Nếu đang dùng H2 (dev mode), skip validation
    if (dbUrl != null && dbUrl.contains("h2")) {
      log.info("Đang sử dụng H2 database - bỏ qua validation database");
      return;
    }
    
    String dbPassword = environment.getProperty("spring.datasource.password");
    if (dbPassword == null || dbPassword.trim().isEmpty()) {
      if (isProductionProfile()) {
        errors.add("❌ DB_PASSWORD: Mật khẩu database không được để trống");
      } else {
        log.warn("⚠️ DB_PASSWORD: Mật khẩu database không được cấu hình - có thể gây lỗi kết nối");
      }
    }

    if (dbUrl == null || dbUrl.trim().isEmpty()) {
      errors.add("❌ DB_URL: URL database không được để trống");
    } else if (!dbUrl.startsWith("jdbc:postgresql://")) {
      errors.add("⚠️ DB_URL: URL database không đúng format (phải là PostgreSQL)");
    }
  }

  private void validateJwtConfig(List<String> errors) {
    String jwtSecret = environment.getProperty("jwt.secret");
    if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
      errors.add("❌ JWT_SECRET: JWT secret không được để trống");
    } else if (jwtSecret.length() < 32) {
      errors.add("❌ JWT_SECRET: JWT secret phải có ít nhất 32 ký tự");
    } else if (jwtSecret.equals("your-256-bit-secret-key-change-in-production-minimum-32-characters")) {
      // Chỉ bắt buộc trong production, các môi trường khác chỉ warning
      if (isProductionProfile()) {
        errors.add("❌ JWT_SECRET: JWT secret không được sử dụng giá trị mặc định trong production");
      } else {
        log.warn("⚠️ JWT_SECRET: Đang sử dụng giá trị mặc định - nên thay đổi trong production");
      }
    }
  }

  private void validateProductionConfig(List<String> errors) {
    // Validate log file path
    String logFilePath = environment.getProperty("logging.file.name");
    if (logFilePath == null || logFilePath.trim().isEmpty()) {
      errors.add("⚠️ LOG_FILE_PATH: Đường dẫn file log không được để trống trong production");
    }

    // Validate frontend URL
    String frontendUrl = environment.getProperty("app.frontend.url");
    if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
      errors.add("⚠️ FRONTEND_URL: Frontend URL không được để trống trong production");
    } else if (!frontendUrl.startsWith("https://")) {
      errors.add("⚠️ FRONTEND_URL: Frontend URL trong production phải sử dụng HTTPS");
    }

    // Validate CORS origins
    String corsOrigins = environment.getProperty("app.cors.allowed-origins");
    if (corsOrigins == null || corsOrigins.trim().isEmpty()) {
      errors.add("⚠️ CORS_ALLOWED_ORIGINS: CORS origins không được để trống trong production");
    } else if (corsOrigins.contains("localhost") || corsOrigins.contains("127.0.0.1")) {
      errors.add("⚠️ CORS_ALLOWED_ORIGINS: Không nên sử dụng localhost trong production");
    }
  }

  private void validateMailConfig(List<String> errors) {
    String smtpUsername = environment.getProperty("spring.mail.username");
    String smtpPassword = environment.getProperty("spring.mail.password");

    if (smtpUsername == null || smtpUsername.trim().isEmpty()) {
      log.warn("⚠️ SMTP_USERNAME: Không được cấu hình - tính năng email sẽ không hoạt động");
    }

    if (smtpPassword == null || smtpPassword.trim().isEmpty()) {
      log.warn("⚠️ SMTP_PASSWORD: Không được cấu hình - tính năng email sẽ không hoạt động");
    }
  }

  private void validateRedisConfig(List<String> errors) {
    String redisHost = environment.getProperty("spring.data.redis.host");
    if (redisHost == null || redisHost.trim().isEmpty()) {
      errors.add("⚠️ REDIS_HOST: Redis host không được để trống khi rate limiting sử dụng Redis");
    }
  }

  private boolean isProductionProfile() {
    return activeProfile != null && activeProfile.contains("prod");
  }

  private boolean isRateLimitingWithRedis() {
    String enabled = environment.getProperty("app.rate-limiting.enabled", "true");
    String useRedis = environment.getProperty("app.rate-limiting.use-redis", "true");
    return "true".equalsIgnoreCase(enabled) && "true".equalsIgnoreCase(useRedis);
  }
}
