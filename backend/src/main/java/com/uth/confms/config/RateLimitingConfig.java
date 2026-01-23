package com.uth.confms.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Rate Limiting sử dụng Bucket4j và Redis
 *
 * <p>Rate limiting được sử dụng để bảo vệ API khỏi:
 * <ul>
 *   <li>Tấn công brute force
 *   <li>Lạm dụng API
 *   <li>DDoS attacks
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(name = "app.rate-limiting.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingConfig {

  @Value("${app.rate-limiting.use-redis:true}")
  private boolean useRedis;

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Bean
  @ConditionalOnProperty(name = "app.rate-limiting.use-redis", havingValue = "true", matchIfMissing = true)
  public LettuceBasedProxyManager<byte[]> proxyManager() {
    if (!useRedis) {
      return null;
    }

    try {
      RedisURI.Builder uriBuilder = RedisURI.builder()
          .withHost(redisHost)
          .withPort(redisPort);

      if (redisPassword != null && !redisPassword.isEmpty()) {
        uriBuilder.withPassword(redisPassword.toCharArray());
      }

      RedisClient redisClient = RedisClient.create(uriBuilder.build());
      return LettuceBasedProxyManager.builderFor(redisClient)
          .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
              Duration.ofSeconds(10)))
          .build();
    } catch (Exception e) {
      // Nếu không kết nối được Redis, trả về null để dùng in-memory
      return null;
    }
  }
}
