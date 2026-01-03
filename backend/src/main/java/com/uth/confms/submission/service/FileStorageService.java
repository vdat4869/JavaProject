package com.uth.confms.submission.service;

import com.uth.confms.submission.config.StorageProperties;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final StorageProperties storageProperties;
    private MinioClient minioClient;
    
    /**
     * Lưu file và trả về đường dẫn lưu trữ
     */
    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        if (storageProperties.getType().equals("minio")) {
            return storeFileMinIO(file, subdirectory);
        } else {
            return storeFileLocal(file, subdirectory);
        }
    }
    
    /**
     * Lấy InputStream của file để download
     */
    public InputStream getFileInputStream(String filePath) throws IOException {
        if (storageProperties.getType().equals("minio")) {
            return getFileInputStreamMinIO(filePath);
        } else {
            return getFileInputStreamLocal(filePath);
        }
    }
    
    /**
     * Xóa file
     */
    public void deleteFile(String filePath) throws IOException {
        if (storageProperties.getType().equals("minio")) {
            deleteFileMinIO(filePath);
        } else {
            deleteFileLocal(filePath);
        }
    }
    
    /**
     * Kiểm tra file có tồn tại không
     */
    public boolean fileExists(String filePath) {
        if (storageProperties.getType().equals("minio")) {
            return fileExistsMinIO(filePath);
        } else {
            return fileExistsLocal(filePath);
        }
    }
    
    // ========== Local Storage Implementation ==========
    
    private String storeFileLocal(MultipartFile file, String subdirectory) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("File name is empty");
        }
        
        // Tạo thư mục nếu chưa tồn tại
        Path basePath = Paths.get(storageProperties.getLocal().getBasePath(), subdirectory);
        Files.createDirectories(basePath);
        
        // Tạo tên file unique
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetPath = basePath.resolve(uniqueFileName);
        
        // Lưu file
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Trả về relative path
        return Paths.get(subdirectory, uniqueFileName).toString().replace("\\", "/");
    }
    
    private InputStream getFileInputStreamLocal(String filePath) throws IOException {
        Path fullPath = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
        return Files.newInputStream(fullPath);
    }
    
    private void deleteFileLocal(String filePath) throws IOException {
        Path fullPath = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
        Files.deleteIfExists(fullPath);
    }
    
    private boolean fileExistsLocal(String filePath) {
        Path fullPath = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
        return Files.exists(fullPath);
    }
    
    // ========== MinIO Implementation ==========
    
    private MinioClient getMinioClient() {
        if (minioClient == null) {
            StorageProperties.MinIOStorage minioConfig = storageProperties.getMinio();
            minioClient = MinioClient.builder()
                    .endpoint(minioConfig.getEndpoint())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();
            
            // Tạo bucket nếu chưa tồn tại
            try {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build());
                }
            } catch (Exception e) {
                log.error("Error initializing MinIO bucket", e);
            }
        }
        return minioClient;
    }
    
    private String storeFileMinIO(MultipartFile file, String subdirectory) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IOException("File name is empty");
            }
            
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            String objectName = subdirectory + "/" + uniqueFileName;
            
            MinioClient client = getMinioClient();
            StorageProperties.MinIOStorage minioConfig = storageProperties.getMinio();
            
            client.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            return objectName;
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Error storing file to MinIO", e);
            throw new IOException("Failed to store file to MinIO", e);
        }
    }
    
    private InputStream getFileInputStreamMinIO(String filePath) throws IOException {
        try {
            MinioClient client = getMinioClient();
            StorageProperties.MinIOStorage minioConfig = storageProperties.getMinio();
            
            return client.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Error getting file from MinIO", e);
            throw new IOException("Failed to get file from MinIO", e);
        }
    }
    
    private void deleteFileMinIO(String filePath) throws IOException {
        try {
            MinioClient client = getMinioClient();
            StorageProperties.MinIOStorage minioConfig = storageProperties.getMinio();
            
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Error deleting file from MinIO", e);
            throw new IOException("Failed to delete file from MinIO", e);
        }
    }
    
    private boolean fileExistsMinIO(String filePath) {
        try {
            MinioClient client = getMinioClient();
            StorageProperties.MinIOStorage minioConfig = storageProperties.getMinio();
            
            client.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ========== Helper Methods ==========
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
}




