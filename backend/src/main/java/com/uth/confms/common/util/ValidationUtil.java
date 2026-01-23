package com.uth.confms.common.util;

import java.util.regex.Pattern;

/**
 * Utility class cho các validation operations
 *
 * <p>Provides methods for:
 * <ul>
 *   <li>Email validation
 *   <li>Phone number validation (Vietnamese và international)
 *   <li>URL validation
 *   <li>Password strength validation
 *   <li>String null/empty checks
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 2.0
 */
public class ValidationUtil {
  
  // Email pattern: allows most common email formats
  private static final Pattern EMAIL_PATTERN = 
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  
  // Vietnamese phone: 10 digits, starts with 0 or +84
  // Examples: 0912345678, +84912345678, 0123456789
  private static final Pattern VIETNAMESE_PHONE_PATTERN = 
      Pattern.compile("^(\\+84|0)[1-9][0-9]{8,9}$");
  
  // International phone: + followed by country code and number
  private static final Pattern INTERNATIONAL_PHONE_PATTERN = 
      Pattern.compile("^\\+[1-9][0-9]{1,14}$");
  
  // URL pattern: http/https URLs
  private static final Pattern URL_PATTERN = 
      Pattern.compile("^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$");
  
  // Password: at least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
  private static final Pattern STRONG_PASSWORD_PATTERN = 
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
  
  // Password: at least 6 chars (weak validation)
  private static final Pattern WEAK_PASSWORD_PATTERN = 
      Pattern.compile("^.{6,}$");

  /**
   * Validate email address
   *
   * @param email Email cần validate
   * @return true nếu email hợp lệ
   */
  public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email.trim()).matches();
  }

  /**
   * Validate Vietnamese phone number
   *
   * <p>Accepts formats:
   * <ul>
   *   <li>0912345678 (10 digits, starts with 0)
   *   <li>+84912345678 (international format)
   * </ul>
   *
   * @param phone Phone number cần validate
   * @return true nếu phone hợp lệ
   */
  public static boolean isValidVietnamesePhone(String phone) {
    if (phone == null || phone.trim().isEmpty()) {
      return false;
    }
    // Remove spaces and dashes
    String cleaned = phone.trim().replaceAll("[\\s-]", "");
    return VIETNAMESE_PHONE_PATTERN.matcher(cleaned).matches();
  }

  /**
   * Validate international phone number
   *
   * @param phone Phone number cần validate
   * @return true nếu phone hợp lệ
   */
  public static boolean isValidInternationalPhone(String phone) {
    if (phone == null || phone.trim().isEmpty()) {
      return false;
    }
    String cleaned = phone.trim().replaceAll("[\\s-]", "");
    return INTERNATIONAL_PHONE_PATTERN.matcher(cleaned).matches();
  }

  /**
   * Validate phone number (Vietnamese hoặc international)
   *
   * @param phone Phone number cần validate
   * @return true nếu phone hợp lệ
   */
  public static boolean isValidPhone(String phone) {
    return isValidVietnamesePhone(phone) || isValidInternationalPhone(phone);
  }

  /**
   * Validate URL (http/https)
   *
   * @param url URL cần validate
   * @return true nếu URL hợp lệ
   */
  public static boolean isValidUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return false;
    }
    return URL_PATTERN.matcher(url.trim()).matches();
  }

  /**
   * Validate password strength (strong)
   *
   * <p>Requirements:
   * <ul>
   *   <li>At least 8 characters
   *   <li>At least 1 uppercase letter
   *   <li>At least 1 lowercase letter
   *   <li>At least 1 digit
   *   <li>At least 1 special character (@$!%*?&)
   * </ul>
   *
   * @param password Password cần validate
   * @return true nếu password đủ mạnh
   */
  public static boolean isStrongPassword(String password) {
    if (password == null || password.isEmpty()) {
      return false;
    }
    return STRONG_PASSWORD_PATTERN.matcher(password).matches();
  }

  /**
   * Validate password (weak validation - chỉ check độ dài)
   *
   * @param password Password cần validate
   * @return true nếu password có ít nhất 6 ký tự
   */
  public static boolean isValidPassword(String password) {
    if (password == null || password.isEmpty()) {
      return false;
    }
    return WEAK_PASSWORD_PATTERN.matcher(password).matches();
  }

  /**
   * Get password strength level
   *
   * @param password Password cần đánh giá
   * @return "STRONG", "MEDIUM", "WEAK", hoặc "INVALID"
   */
  public static String getPasswordStrength(String password) {
    if (password == null || password.isEmpty()) {
      return "INVALID";
    }
    
    if (isStrongPassword(password)) {
      return "STRONG";
    }
    
    // Medium: at least 8 chars với mix of letters và digits
    if (password.length() >= 8 && 
        password.matches(".*[a-zA-Z].*") && 
        password.matches(".*\\d.*")) {
      return "MEDIUM";
    }
    
    // Weak: at least 6 chars
    if (password.length() >= 6) {
      return "WEAK";
    }
    
    return "INVALID";
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
