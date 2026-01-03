package com.uth.confms.submission.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String type = "local"; // local hoặc minio
    
    private LocalStorage local = new LocalStorage();
    private MinIOStorage minio = new MinIOStorage();
    
    @Data
    public static class LocalStorage {
        private String basePath = "./uploads";
    }
    
    @Data
    public static class MinIOStorage {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucketName = "uth-confms";
    }
}




