package com.uth.confms.storage.service;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface cho file storage operations
 *
 * <p>
 * Service này cung cấp các operations để quản lý file storage:
 *
 * <ul>
 * <li>Lưu PDF files cho submissions và camera-ready papers
 * <li>Xóa files
 * <li>Lấy file streams để download
 * </ul>
 *
 * <p>
 * Hiện tại chỉ hỗ trợ:
 *
 * <ul>
 * <li>File types: PDF only
 * <li>Max file size: 20MB
 * <li>Storage mode: Local filesystem
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public interface StorageService {

  /**
   * Lưu PDF file cho submission
   *
   * <p>
   * File sẽ được lưu với path pattern:
   * conferences/{conferenceId}/submissions/{submissionId}/{timestamp}_{originalFilename}
   *
   * @param conferenceId ID của conference
   * @param submissionId ID của submission
   * @param file         MultipartFile chứa PDF data
   * @return Đường dẫn file đã lưu (relative path từ base directory)
   * @throws IllegalArgumentException Nếu file không phải PDF hoặc vượt quá size
   *                                  limit
   * @throws RuntimeException         Nếu có lỗi khi lưu file
   */
  // Lưu file PDF submission
  String storeSubmissionPdf(Long conferenceId, Long submissionId, MultipartFile file);

  /**
   * Lưu PDF file cho camera-ready paper
   *
   * <p>
   * File sẽ được lưu với path pattern:
   * conferences/{conferenceId}/camera-ready/{paperId}/{timestamp}_{originalFilename}
   *
   * @param conferenceId ID của conference
   * @param paperId      ID của camera-ready paper (submission ID)
   * @param file         MultipartFile chứa PDF data
   * @return Đường dẫn file đã lưu (relative path từ base directory)
   * @throws IllegalArgumentException Nếu file không phải PDF hoặc vượt quá size
   *                                  limit
   * @throws RuntimeException         Nếu có lỗi khi lưu file
   */
  // Lưu file PDF camera-ready
  String storeCameraReadyPdf(Long conferenceId, Long paperId, MultipartFile file);

  /**
   * Xóa file từ storage
   *
   * @param filePath Đường dẫn file (relative path từ base directory)
   * @return true nếu xóa thành công, false nếu file không tồn tại
   */
  // Xóa file
  boolean deleteFile(String filePath);

  /**
   * Lấy InputStream để đọc file
   *
   * @param filePath Đường dẫn file (relative path từ base directory)
   * @return InputStream để đọc file
   * @throws RuntimeException Nếu file không tồn tại hoặc có lỗi khi đọc
   */
  // Lấy luồng đọc file
  InputStream getFileStream(String filePath);

  /**
   * Kiểm tra file có tồn tại không
   *
   * @param filePath Đường dẫn file (relative path từ base directory)
   * @return true nếu file tồn tại, false nếu không
   */
  // Kiểm tra file tồn tại
  boolean fileExists(String filePath);

  /**
   * Lấy kích thước file (bytes)
   *
   * @param filePath Đường dẫn file (relative path từ base directory)
   * @return Kích thước file tính bằng bytes
   * @throws RuntimeException Nếu file không tồn tại
   */
  // Lấy kích thước file
  long getFileSize(String filePath);

  /**
   * Verify checksum của file để detect corruption
   *
   * @param filePath         Đường dẫn file (relative path từ base directory)
   * @param expectedChecksum SHA-256 checksum mong đợi
   * @return true nếu checksum khớp, false nếu không khớp hoặc có lỗi
   */
  // Kiểm tra checksum file
  boolean verifyChecksum(String filePath, String expectedChecksum);
}
