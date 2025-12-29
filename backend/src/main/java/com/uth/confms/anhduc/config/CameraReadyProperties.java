package com.uth.confms.anhduc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cấu hình cho module Camera-ready.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.camera-ready")
public class CameraReadyProperties {

    private Storage storage = new Storage();
    private Pdf pdf = new Pdf();

    @Data
    public static class Storage {
        /**
         * Loại lưu trữ: local hoặc minio
         */
        private String type = "local";
        
        /**
         * Đường dẫn lưu trữ local
         */
        private String localPath = "./storage/camera-ready";
        
        private Minio minio = new Minio();

        @Data
        public static class Minio {
            private String endpoint = "http://localhost:9000";
            private String accessKey = "minioadmin";
            private String secretKey = "minioadmin";
            private String bucket = "camera-ready";
        }
    }

    @Data
    public static class Pdf {
        /**
         * Kích thước file tối đa (bytes) - mặc định 10MB
         */
        private long maxFileSize = 10485760L;
        
        /**
         * Số trang tối đa
         */
        private int maxPages = 12;
        
        /**
         * Các kích thước trang được phép
         */
        private List<String> allowedPageSizes = List.of("A4", "LETTER");
        
        /**
         * Kiểm tra JavaScript trong PDF
         */
        private boolean checkJavascript = true;
        
        /**
         * Kiểm tra file đính kèm trong PDF
         */
        private boolean checkEmbeddedFiles = true;
    }
}
