package com.uth.confms.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class cho file operations và validation
 *
 * <p>Provides methods for:
 * <ul>
 *   <li>PDF file validation (content type, size, extension)
 *   <li>File size validation
 *   <li>File extension validation
 *   <li>Checksum calculation (SHA-256)
 *   <li>Filename sanitization (delegates to StringUtil)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class FileUtil {

  public static final String PDF_CONTENT_TYPE = "application/pdf";
  public static final String PDF_EXTENSION = ".pdf";
  public static final List<String> ALLOWED_PDF_EXTENSIONS = Arrays.asList(".pdf", ".PDF");

  /**
   * Validate PDF file: content type, size, extension
   *
   * @param file File cần validate
   * @param maxSizeMB Kích thước tối đa (MB)
   * @throws IllegalArgumentException Nếu file không hợp lệ
   */
  public static void validatePdfFile(MultipartFile file, long maxSizeMB) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is null or empty");
    }

    // Check content type
    String contentType = file.getContentType();
    if (contentType == null || !contentType.equals(PDF_CONTENT_TYPE)) {
      throw new IllegalArgumentException(
          "Only PDF files are allowed. Received content type: " + contentType);
    }

    // Check file size
    validateFileSize(file, maxSizeMB);

    // Check filename extension
    String filename = file.getOriginalFilename();
    if (filename == null || !hasPdfExtension(filename)) {
      throw new IllegalArgumentException("File must have .pdf extension");
    }
  }

  /**
   * Validate file size
   *
   * @param file File cần validate
   * @param maxSizeMB Kích thước tối đa (MB)
   * @throws IllegalArgumentException Nếu file quá lớn
   */
  public static void validateFileSize(MultipartFile file, long maxSizeMB) {
    if (file == null) {
      throw new IllegalArgumentException("File is null");
    }

    long fileSize = file.getSize();
    long maxSizeBytes = maxSizeMB * 1024 * 1024;

    if (fileSize > maxSizeBytes) {
      throw new IllegalArgumentException(
          String.format(
              "File size exceeds maximum allowed size. Max: %d MB, Actual: %.2f MB",
              maxSizeMB, fileSize / (1024.0 * 1024.0)));
    }
  }

  /**
   * Validate file extension
   *
   * @param filename Tên file
   * @param allowedExtensions Danh sách extensions được phép (ví dụ: [".pdf", ".doc"])
   * @throws IllegalArgumentException Nếu extension không hợp lệ
   */
  public static void validateFileExtension(String filename, List<String> allowedExtensions) {
    if (filename == null || filename.trim().isEmpty()) {
      throw new IllegalArgumentException("Filename is null or empty");
    }

    String extension = getFileExtension(filename);
    if (extension == null || !allowedExtensions.contains(extension)) {
      throw new IllegalArgumentException(
          String.format(
              "File extension '%s' is not allowed. Allowed extensions: %s",
              extension, allowedExtensions));
    }
  }

  /**
   * Check nếu file có PDF extension
   *
   * @param filename Tên file
   * @return true nếu có .pdf extension
   */
  public static boolean hasPdfExtension(String filename) {
    if (filename == null) {
      return false;
    }
    String extension = getFileExtension(filename);
    return extension != null && ALLOWED_PDF_EXTENSIONS.contains(extension);
  }

  /**
   * Lấy file extension từ filename
   *
   * @param filename Tên file
   * @return Extension (ví dụ: ".pdf") hoặc null nếu không có
   */
  public static String getFileExtension(String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      return null;
    }

    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      return null;
    }

    return filename.substring(lastDotIndex);
  }

  /**
   * Sanitize filename (delegates to StringUtil)
   *
   * @param filename Tên file gốc
   * @return Tên file đã được sanitize
   */
  public static String sanitizeFilename(String filename) {
    return StringUtil.sanitizeFilename(filename);
  }

  /**
   * Sanitize filename và đảm bảo có extension
   *
   * @param filename Tên file gốc
   * @param defaultExtension Extension mặc định (ví dụ: ".pdf")
   * @return Tên file đã được sanitize với extension
   */
  public static String sanitizeFilename(String filename, String defaultExtension) {
    return StringUtil.sanitizeFilename(filename, defaultExtension);
  }

  /**
   * Calculate SHA-256 checksum của file
   *
   * @param file File cần calculate checksum
   * @return SHA-256 checksum (hex string)
   * @throws IOException Nếu không đọc được file
   */
  public static String calculateChecksum(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is null or empty");
    }

    try (InputStream inputStream = file.getInputStream()) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }

      byte[] hash = digest.digest();
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }

  /**
   * Calculate SHA-256 checksum từ InputStream
   *
   * @param inputStream InputStream cần calculate checksum
   * @return SHA-256 checksum (hex string)
   * @throws IOException Nếu không đọc được stream
   */
  public static String calculateChecksum(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      throw new IllegalArgumentException("InputStream is null");
    }

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }

      byte[] hash = digest.digest();
      return bytesToHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }

  /**
   * Convert byte array to hex string
   *
   * @param bytes Byte array
   * @return Hex string
   */
  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  /**
   * Get file size in MB
   *
   * @param file File
   * @return Size in MB
   */
  public static double getFileSizeMB(MultipartFile file) {
    if (file == null) {
      return 0.0;
    }
    return file.getSize() / (1024.0 * 1024.0);
  }

  /**
   * Get file size in KB
   *
   * @param file File
   * @return Size in KB
   */
  public static double getFileSizeKB(MultipartFile file) {
    if (file == null) {
      return 0.0;
    }
    return file.getSize() / 1024.0;
  }
}
