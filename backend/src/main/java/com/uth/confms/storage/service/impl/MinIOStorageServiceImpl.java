package com.uth.confms.storage.service.impl;

import com.uth.confms.common.util.FileUtil;
import com.uth.confms.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation của StorageService sử dụng MinIO (S3-compatible object storage)
 *
 * <p>Service này lưu files vào MinIO với cấu trúc tương tự S3:
 *
 * <ul>
 *   <li>Bucket: Configurable via app.storage.s3.bucket
 *   <li>Submission files: conferences/{conferenceId}/submissions/{submissionId}/{timestamp}_{filename}.pdf
 *   <li>Camera-ready files: conferences/{conferenceId}/camera-ready/{paperId}/{timestamp}_{filename}.pdf
 * </ul>
 *
 * <p>MinIO sử dụng S3-compatible API, nên implementation này tương tự S3StorageServiceImpl
 * nhưng với path-style access enabled và custom endpoint.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.backend", havingValue = "minio", matchIfMissing = false)
public class MinIOStorageServiceImpl implements StorageService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final S3Client s3Client;
    private final String bucket;
    private final long maxFileSizeMB;

    public MinIOStorageServiceImpl(
            @Value("${app.storage.s3.bucket:}") String bucket,
            @Value("${app.storage.max-file-size-mb:20}") long maxFileSizeMB,
            @Value("${app.storage.s3.endpoint:}") String endpoint,
            @Value("${app.storage.s3.region:us-east-1}") String region,
            @Value("${app.storage.s3.access-key:}") String accessKey,
            @Value("${app.storage.s3.secret-key:}") String secretKey) {
        
        this.bucket = bucket;
        this.maxFileSizeMB = maxFileSizeMB;
        
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("MinIO bucket name is required (app.storage.s3.bucket)");
        }
        
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("MinIO endpoint is required (app.storage.s3.endpoint)");
        }
        
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("MinIO access key and secret key are required");
        }
        
        // Build S3Client for MinIO (S3-compatible)
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(
                        software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                                software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true) // MinIO requires path-style access
                .build();
        
        // Verify bucket exists
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.info("MinIO storage initialized with bucket: {} at endpoint: {}", bucket, endpoint);
        } catch (NoSuchBucketException e) {
            log.error("MinIO bucket does not exist: {}", bucket);
            throw new RuntimeException("MinIO bucket does not exist: " + bucket, e);
        } catch (Exception e) {
            log.error("Error verifying MinIO bucket: {}", bucket, e);
            throw new RuntimeException("Error verifying MinIO bucket: " + bucket, e);
        }
    }

    @Override
    public String storeSubmissionPdf(Long conferenceId, Long submissionId, MultipartFile file) {
        FileUtil.validatePdfFile(file, maxFileSizeMB);
        String key = String.format(
                "conferences/%d/submissions/%d/%s_%s",
                conferenceId,
                submissionId,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                FileUtil.sanitizeFilename(file.getOriginalFilename(), FileUtil.PDF_EXTENSION));
        return storeFile(key, file);
    }

    @Override
    public String storeCameraReadyPdf(Long conferenceId, Long paperId, MultipartFile file) {
        FileUtil.validatePdfFile(file, maxFileSizeMB);
        String key = String.format(
                "conferences/%d/camera-ready/%d/%s_%s",
                conferenceId,
                paperId,
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                FileUtil.sanitizeFilename(file.getOriginalFilename(), FileUtil.PDF_EXTENSION));
        return storeFile(key, file);
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(filePath)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("Deleted file from MinIO: {}", filePath);
            return true;
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", filePath, e);
            return false;
        }
    }

    @Override
    public InputStream getFileStream(String filePath) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(filePath)
                    .build();
            return s3Client.getObject(getRequest);
        } catch (NoSuchKeyException e) {
            log.error("File not found in MinIO: {}", filePath);
            throw new RuntimeException("File not found: " + filePath, e);
        } catch (Exception e) {
            log.error("Error reading file from MinIO: {}", filePath, e);
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(filePath)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence in MinIO: {}", filePath, e);
            return false;
        }
    }

    @Override
    public long getFileSize(String filePath) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(filePath)
                    .build();
            HeadObjectResponse response = s3Client.headObject(headRequest);
            return response.contentLength();
        } catch (NoSuchKeyException e) {
            log.error("File not found in MinIO: {}", filePath);
            throw new RuntimeException("File not found: " + filePath, e);
        } catch (Exception e) {
            log.error("Error getting file size from MinIO: {}", filePath, e);
            throw new RuntimeException("Error getting file size: " + filePath, e);
        }
    }

    @Override
    public boolean verifyChecksum(String filePath, String expectedChecksum) {
        try {
            InputStream inputStream = getFileStream(filePath);
            String actualChecksum = FileUtil.calculateChecksum(inputStream);
            inputStream.close();
            
            boolean matches = actualChecksum.equalsIgnoreCase(expectedChecksum);
            if (!matches) {
                log.warn("Checksum mismatch for file: {} (expected: {}, actual: {})", 
                        filePath, expectedChecksum, actualChecksum);
            }
            return matches;
        } catch (Exception e) {
            log.error("Error verifying checksum for file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Lưu file vào MinIO
     *
     * @param key MinIO object key
     * @param file MultipartFile cần lưu
     * @return MinIO object key
     */
    private String storeFile(String key, MultipartFile file) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/pdf")
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Stored file to MinIO: {} (size: {} bytes)", key, file.getSize());
            return key;
        } catch (IOException e) {
            log.error("Error storing file to MinIO: {}", key, e);
            throw new RuntimeException("Error storing file: " + key, e);
        }
    }
}
