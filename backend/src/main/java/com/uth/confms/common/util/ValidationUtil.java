package com.uth.confms.common.util;

import java.util.regex.Pattern;

public class ValidationUtil {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }
}
