package com.uth.confms;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableRetry
public class ConfmsApplication {

  @jakarta.annotation.PostConstruct
  public void init() {
    // Set TimeZone to Asia/Ho_Chi_Minh (UTC+7)
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
  }

  public static void main(String[] args) {
    // Load .env file if it exists
    try {
      Dotenv dotenv = null;
      String userDir = System.getProperty("user.dir");

      // Try multiple locations for .env file
      String[] searchPaths = {
          ".", // Current directory
          userDir, // User directory (usually backend/ when running from Maven)
          userDir + "/backend", // Explicit backend subdirectory
          "../", // Parent directory
          "../backend" // Backend in parent
      };

      for (String path : searchPaths) {
        try {
          java.io.File envFile = new java.io.File(path, ".env");
          if (envFile.exists() && envFile.isFile()) {
            dotenv = Dotenv.configure()
                .directory(path)
                .ignoreIfMissing()
                .load();
            System.out.println("✅ Found .env file at: " + envFile.getAbsolutePath());
            break;
          }
        } catch (Exception e) {
          // Continue to next path
        }
      }

      if (dotenv == null) {
        // Last try: use default behavior (current directory)
        try {
          dotenv = Dotenv.configure()
              .ignoreIfMissing()
              .load();
        } catch (Exception e) {
          // Ignore
        }
      }

      if (dotenv != null) {
        // Set system properties from .env file
        int loadedCount = 0;
        for (var entry : dotenv.entries()) {
          String key = entry.getKey();
          String value = entry.getValue();
          if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
            loadedCount++;
          }
        }
        System.out.println("✅ Loaded " + loadedCount + " variables from .env file");
      } else {
        System.out.println("⚠️ Note: .env file not found. Using environment variables and defaults.");
        System.out.println("   Searched in: " + String.join(", ", searchPaths));
      }
    } catch (Exception e) {
      // .env file not found or error loading - continue without it
      System.out.println("⚠️ Note: .env file not found or could not be loaded: " + e.getMessage());
      System.out.println("   Using environment variables and defaults.");
    }

    SpringApplication.run(ConfmsApplication.class, args);
  }
}
