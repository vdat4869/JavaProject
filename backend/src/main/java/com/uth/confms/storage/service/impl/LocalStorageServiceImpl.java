package com.uth.confms.storage.service.impl;

import com.uth.confms.common.util.FileUtil;
import com.uth.confms.storage.service.StorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation của StorageService sử dụng local filesystem
 *
 * <p>Service này lưu files vào local filesystem với cấu trúc:
 *
 * <ul>
 *   <li>Base directory: /data/uploads (có thể config qua application.yml)
 *   <li>Submission files: submissions/{submissionId}/{timestamp}_{filename}.pdf
 *   <li>Camera-ready files: camera-ready/{paperId}/{timestamp}_{filename}.pdf
 * </ul>
 *
 * <p>Validation:
 *
 * <ul>
 *   <li>Chỉ chấp nhận PDF files (content-type: application/pdf)
 *   <li>Max file size: 20MB
 *   <li>Tự động tạo directories nếu chưa tồn tại
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@ConditionalOnProperty(name = "app.storage.backend", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {

  private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  @Value("${app.storage.base-dir:/data/uploads}")
  private String baseDir;

  @Value("${app.storage.max-file-size-mb:20}")
  private long maxFileSizeMB;

  @Override
  public String storeSubmissionPdf(Long conferenceId, Long submissionId, MultipartFile file) {
    FileUtil.validatePdfFile(file, maxFileSizeMB);
    String relativePath =
        String.format(
            "conferences/%d/submissions/%d/%s_%s",
            conferenceId,
            submissionId,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            FileUtil.sanitizeFilename(file.getOriginalFilename(), FileUtil.PDF_EXTENSION));
    return storeFile(relativePath, file);
  }

  @Override
  public String storeCameraReadyPdf(Long conferenceId, Long paperId, MultipartFile file) {
    FileUtil.validatePdfFile(file, maxFileSizeMB);
    String relativePath =
        String.format(
            "conferences/%d/camera-ready/%d/%s_%s",
            conferenceId,
            paperId,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            FileUtil.sanitizeFilename(file.getOriginalFilename(), FileUtil.PDF_EXTENSION));
    return storeFile(relativePath, file);
  }

  @Override
  public boolean deleteFile(String filePath) {
    try {
      Path path = getFullPath(filePath);
      if (Files.exists(path)) {
        Files.delete(path);
        log.info("Deleted file: {}", filePath);
        return true;
      }
      log.warn("File not found for deletion: {}", filePath);
      return false;
    } catch (IOException e) {
      log.error("Error deleting file: {}", filePath, e);
      return false;
    }
  }

  @Override
  public InputStream getFileStream(String filePath) {
    try {
      Path path = getFullPath(filePath);
      if (!Files.exists(path)) {
        throw new RuntimeException("File not found: " + filePath);
      }
      return Files.newInputStream(path);
    } catch (IOException e) {
      log.error("Error reading file: {}", filePath, e);
      throw new RuntimeException("Error reading file: " + filePath, e);
    }
  }

  @Override
  public boolean fileExists(String filePath) {
    Path path = getFullPath(filePath);
    return Files.exists(path);
  }

  @Override
  public long getFileSize(String filePath) {
    try {
      Path path = getFullPath(filePath);
      if (!Files.exists(path)) {
        throw new RuntimeException("File not found: " + filePath);
      }
      return Files.size(path);
    } catch (IOException e) {
      log.error("Error getting file size: {}", filePath, e);
      throw new RuntimeException("Error getting file size: " + filePath, e);
    }
  }

  /**
   * Lưu file vào storage
   *
   * @param relativePath Đường dẫn relative từ base directory
   * @param file MultipartFile cần lưu
   * @return Đường dẫn relative của file đã lưu
   */
  private String storeFile(String relativePath, MultipartFile file) {
    try {
      Path fullPath = getFullPath(relativePath);

      // Tạo parent directories nếu chưa tồn tại
      Files.createDirectories(fullPath.getParent());

      // Lưu file
      Files.copy(file.getInputStream(), fullPath, StandardCopyOption.REPLACE_EXISTING);

      log.info("Stored file: {} (size: {} bytes)", relativePath, file.getSize());
      return relativePath;
    } catch (IOException e) {
      log.error("Error storing file: {}", relativePath, e);
      throw new RuntimeException("Error storing file: " + relativePath, e);
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
   * Lấy base directory (for backup service access)
   *
   * @return Base directory path
   */
  public String getBaseDir() {
    return baseDir;
  }

  /**
   * Lấy full path từ relative path
   *
   * @param relativePath Đường dẫn relative
   * @return Full Path object
   */
  private Path getFullPath(String relativePath) {
    return Paths.get(baseDir, relativePath).normalize();
  }
}
