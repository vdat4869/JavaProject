package com.uth.confms.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class cho các operations trên String
 *
 * <p>Provides methods for:
 * <ul>
 *   <li>String normalization (trim, lowercase, remove accents)
 *   <li>Filename sanitization (remove path traversal, special chars)
 *   <li>Slug generation (URL-friendly strings)
 *   <li>String truncation với ellipsis
 *   <li>Capitalization
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class StringUtil {

  private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
  private static final Pattern PATH_TRAVERSAL = Pattern.compile("\\.\\.|[/\\\\]");
  private static final Pattern SPECIAL_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

  /**
   * Normalize string: trim, lowercase, remove accents
   *
   * @param str String cần normalize
   * @return String đã được normalize
   */
  public static String normalize(String str) {
    if (str == null) {
      return null;
    }
    return removeAccents(str.trim().toLowerCase(Locale.ENGLISH));
  }

  /**
   * Remove accents/diacritics từ string
   *
   * @param str String cần remove accents
   * @return String không có accents
   */
  public static String removeAccents(String str) {
    if (str == null) {
      return null;
    }
    String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
    return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  /**
   * Sanitize filename để tránh path traversal và các ký tự không hợp lệ
   *
   * <p>Removes:
   * <ul>
   *   <li>Path traversal attempts (.., /, \)
   *   <li>Special characters (giữ lại a-z, A-Z, 0-9, ., _, -)
   * </ul>
   *
   * @param filename Tên file gốc
   * @return Tên file đã được sanitize
   */
  public static String sanitizeFilename(String filename) {
    if (filename == null || filename.trim().isEmpty()) {
      return "file";
    }

    // Remove path traversal attempts
    String sanitized = PATH_TRAVERSAL.matcher(filename).replaceAll("_");

    // Remove special characters (keep only alphanumeric, dot, underscore, hyphen)
    sanitized = SPECIAL_CHARS.matcher(sanitized).replaceAll("_");

    // Remove multiple consecutive underscores
    sanitized = sanitized.replaceAll("_{2,}", "_");

    // Remove leading/trailing underscores and dots
    sanitized = sanitized.replaceAll("^[._]+|[._]+$", "");

    // Ensure not empty
    if (sanitized.isEmpty()) {
      return "file";
    }

    return sanitized;
  }

  /**
   * Sanitize filename và đảm bảo có extension
   *
   * @param filename Tên file gốc
   * @param defaultExtension Extension mặc định (ví dụ: ".pdf")
   * @return Tên file đã được sanitize với extension
   */
  public static String sanitizeFilename(String filename, String defaultExtension) {
    String sanitized = sanitizeFilename(filename);

    // Ensure extension
    if (defaultExtension != null && !defaultExtension.isEmpty()) {
      String ext = defaultExtension.startsWith(".") ? defaultExtension : "." + defaultExtension;
      if (!sanitized.toLowerCase().endsWith(ext.toLowerCase())) {
        sanitized += ext;
      }
    }

    return sanitized;
  }

  /**
   * Generate URL-friendly slug từ string
   *
   * <p>Converts "Hello World!" → "hello-world"
   *
   * @param str String cần convert
   * @return Slug string
   */
  public static String slugify(String str) {
    if (str == null || str.trim().isEmpty()) {
      return "";
    }

    // Normalize và remove accents
    String normalized = removeAccents(str.trim().toLowerCase(Locale.ENGLISH));

    // Replace whitespace với hyphens
    normalized = WHITESPACE.matcher(normalized).replaceAll("-");

    // Remove non-latin characters
    normalized = NON_LATIN.matcher(normalized).replaceAll("");

    // Remove multiple consecutive hyphens
    normalized = normalized.replaceAll("-{2,}", "-");

    // Remove leading/trailing hyphens
    normalized = normalized.replaceAll("^-+|-+$", "");

    return normalized;
  }

  /**
   * Truncate string với ellipsis nếu quá dài
   *
   * @param str String cần truncate
   * @param maxLength Độ dài tối đa (không tính ellipsis)
   * @return String đã được truncate
   */
  public static String truncate(String str, int maxLength) {
    if (str == null) {
      return null;
    }
    if (str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength) + "...";
  }

  /**
   * Capitalize first letter của string
   *
   * @param str String cần capitalize
   * @return String với chữ cái đầu viết hoa
   */
  public static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase(Locale.ENGLISH) + str.substring(1);
  }

  /**
   * Capitalize first letter của mỗi word
   *
   * @param str String cần capitalize
   * @return String với mỗi word được capitalize
   */
  public static String capitalizeWords(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    String[] words = str.trim().split("\\s+");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < words.length; i++) {
      if (i > 0) {
        result.append(" ");
      }
      result.append(capitalize(words[i]));
    }

    return result.toString();
  }

  /**
   * Check nếu string là null hoặc empty (sau khi trim)
   *
   * @param str String cần check
   * @return true nếu null hoặc empty
   */
  public static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * Check nếu string không null và không empty (sau khi trim)
   *
   * @param str String cần check
   * @return true nếu không null và không empty
   */
  public static boolean isNotNullOrEmpty(String str) {
    return !isNullOrEmpty(str);
  }
}
