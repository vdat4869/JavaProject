package com.uth.confms.storage.service.impl;

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
public class LocalStorageServiceImpl implements StorageService {

  private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

  private static final String PDF_CONTENT_TYPE = "application/pdf";
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  @Value("${app.storage.base-dir:/data/uploads}")
  private String baseDir;

  @Value("${app.storage.max-file-size-mb:20}")
  private long maxFileSizeMB;

  @Override
  public String storeSubmissionPdf(Long submissionId, MultipartFile file) {
    validatePdfFile(file);
    String relativePath =
        String.format(
            "submissions/%d/%s_%s",
            submissionId,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            sanitizeFilename(file.getOriginalFilename()));
    return storeFile(relativePath, file);
  }

  @Override
  public String storeCameraReadyPdf(Long paperId, MultipartFile file) {
    validatePdfFile(file);
    String relativePath =
        String.format(
            "camera-ready/%d/%s_%s",
            paperId,
            LocalDateTime.now().format(TIMESTAMP_FORMATTER),
            sanitizeFilename(file.getOriginalFilename()));
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

  /**
   * Validate PDF file
   *
   * @param file File cần validate
   * @throws IllegalArgumentException Nếu file không hợp lệ
   */
  private void validatePdfFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is null or empty");
    }

    // Check content type
    String contentType = file.getContentType();
    if (contentType == null || !contentType.equals(PDF_CONTENT_TYPE)) {
      throw new IllegalArgumentException("Only PDF files are allowed. Received: " + contentType);
    }

    // Check file size
    long fileSize = file.getSize();
    long maxSize = maxFileSizeMB * 1024 * 1024;
    if (fileSize > maxSize) {
      throw new IllegalArgumentException(
          String.format(
              "File size exceeds maximum allowed size. Max: %d MB, Actual: %.2f MB",
              maxFileSizeMB, fileSize / (1024.0 * 1024.0)));
    }

    // Check filename extension
    String filename = file.getOriginalFilename();
    if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
      throw new IllegalArgumentException("File must have .pdf extension");
    }
  }

  /**
   * Sanitize filename để tránh path traversal và các ký tự không hợp lệ
   *
   * @param filename Tên file gốc
   * @return Tên file đã được sanitize
   */
  private String sanitizeFilename(String filename) {
    if (filename == null) {
      return "file.pdf";
    }

    // Remove path traversal attempts
    String sanitized =
        filename.replaceAll("\\.\\.", "").replaceAll("/", "_").replaceAll("\\\\", "_");

    // Ensure .pdf extension
    if (!sanitized.toLowerCase().endsWith(".pdf")) {
      sanitized += ".pdf";
    }

    return sanitized;
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
