package com.uth.confms.anhduc.config;

import com.uth.confms.anhduc.modules.storage.service.FileStorageService;
import com.uth.confms.anhduc.modules.storage.service.impl.LocalFileStorageService;
import com.uth.confms.anhduc.modules.storage.service.impl.MinioFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Storage Service.
 * 
 * @author Anh Đức
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final CameraReadyProperties properties;

    @Bean
    public FileStorageService fileStorageService() {
        String storageType = properties.getStorage().getType();
        
        if ("minio".equalsIgnoreCase(storageType)) {
            return new MinioFileStorageService(properties);
        }
        
        return new LocalFileStorageService(properties);
    }
}
