package com.uth.confms.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
  private static final DateTimeFormatter DEFAULT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static String format(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DEFAULT_FORMATTER);
  }

  public static String format(LocalDateTime dateTime, String pattern) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DateTimeFormatter.ofPattern(pattern));
  }
}
