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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation của StorageService sử dụng AWS S3 hoặc S3-compatible storage (MinIO)
 *
 * <p>Service này lưu files vào S3/MinIO với cấu trúc:
 *
 * <ul>
 *   <li>Bucket: Configurable via app.storage.s3.bucket
 *   <li>Submission files: conferences/{conferenceId}/submissions/{submissionId}/{timestamp}_{filename}.pdf
 *   <li>Camera-ready files: conferences/{conferenceId}/camera-ready/{paperId}/{timestamp}_{filename}.pdf
 * </ul>
 *
 * <p>Configuration:
 *
 * <ul>
 *   <li>app.storage.backend=s3 hoặc minio
 *   <li>app.storage.s3.endpoint - S3 endpoint (required for MinIO)
 *   <li>app.storage.s3.region - AWS region
 *   <li>app.storage.s3.bucket - Bucket name
 *   <li>app.storage.s3.access-key - Access key
 *   <li>app.storage.s3.secret-key - Secret key
 *   <li>app.storage.s3.path-style-access - Use path-style access (true for MinIO)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.backend", havingValue = "s3", matchIfMissing = false)
public class S3StorageServiceImpl implements StorageService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final S3Client s3Client;
    private final String bucket;
    private final long maxFileSizeMB;

    public S3StorageServiceImpl(
            @Value("${app.storage.s3.bucket:}") String bucket,
            @Value("${app.storage.max-file-size-mb:20}") long maxFileSizeMB,
            @Value("${app.storage.s3.endpoint:}") String endpoint,
            @Value("${app.storage.s3.region:us-east-1}") String region,
            @Value("${app.storage.s3.access-key:}") String accessKey,
            @Value("${app.storage.s3.secret-key:}") String secretKey,
            @Value("${app.storage.s3.path-style-access:false}") boolean pathStyleAccess) {
        
        this.bucket = bucket;
        this.maxFileSizeMB = maxFileSizeMB;
        
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalArgumentException("S3 bucket name is required (app.storage.s3.bucket)");
        }
        
        // Build S3Client
        software.amazon.awssdk.services.s3.S3ClientBuilder builder = S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(region));
        
        // Configure credentials if provided
        if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
            builder.credentialsProvider(
                    software.amazon.awssdk.auth.credentials.StaticCredentialsProvider.create(
                            software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKey, secretKey)));
        }
        
        // Configure endpoint for MinIO or custom S3-compatible storage
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
            builder.forcePathStyle(pathStyleAccess);
        }
        
        this.s3Client = builder.build();
        
        // Verify bucket exists
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build();
            s3Client.headBucket(headBucketRequest);
            log.info("S3 storage initialized with bucket: {}", bucket);
        } catch (NoSuchBucketException e) {
            log.error("S3 bucket does not exist: {}", bucket);
            throw new RuntimeException("S3 bucket does not exist: " + bucket, e);
        } catch (Exception e) {
            log.error("Error verifying S3 bucket: {}", bucket, e);
            throw new RuntimeException("Error verifying S3 bucket: " + bucket, e);
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
            log.info("Deleted file from S3: {}", filePath);
            return true;
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", filePath, e);
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
            log.error("File not found in S3: {}", filePath);
            throw new RuntimeException("File not found: " + filePath, e);
        } catch (Exception e) {
            log.error("Error reading file from S3: {}", filePath, e);
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
            log.error("Error checking file existence in S3: {}", filePath, e);
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
            log.error("File not found in S3: {}", filePath);
            throw new RuntimeException("File not found: " + filePath, e);
        } catch (Exception e) {
            log.error("Error getting file size from S3: {}", filePath, e);
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
     * Lưu file vào S3
     *
     * @param key S3 object key
     * @param file MultipartFile cần lưu
     * @return S3 object key
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

            log.info("Stored file to S3: {} (size: {} bytes)", key, file.getSize());
            return key;
        } catch (IOException e) {
            log.error("Error storing file to S3: {}", key, e);
            throw new RuntimeException("Error storing file: " + key, e);
        }
    }
}
