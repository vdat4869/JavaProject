package com.uth.confms.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class cho các date/time operations
 *
 * <p>Provides methods for:
 * <ul>
 *   <li>Format LocalDateTime với các patterns khác nhau
 *   <li>Parse string to LocalDateTime
 *   <li>Timezone conversion
 *   <li>Date arithmetic (add days, months, etc.)
 *   <li>Relative time formatting ("2 days ago")
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 2.0
 */
public class DateUtil {
  
  private static final DateTimeFormatter DEFAULT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  private static final DateTimeFormatter ISO_FORMATTER =
      DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  
  private static final DateTimeFormatter DATE_ONLY_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  
  private static final DateTimeFormatter TIME_ONLY_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss");

  /**
   * Format LocalDateTime với default pattern (yyyy-MM-dd HH:mm:ss)
   *
   * @param dateTime LocalDateTime cần format
   * @return Formatted string hoặc null nếu input null
   */
  public static String format(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DEFAULT_FORMATTER);
  }

  /**
   * Format LocalDateTime với custom pattern
   *
   * @param dateTime LocalDateTime cần format
   * @param pattern Pattern (ví dụ: "yyyy-MM-dd", "HH:mm:ss")
   * @return Formatted string hoặc null nếu input null
   */
  public static String format(LocalDateTime dateTime, String pattern) {
    if (dateTime == null) {
      return null;
    }
    try {
      return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid date pattern: " + pattern, e);
    }
  }

  /**
   * Format LocalDateTime với ISO format
   *
   * @param dateTime LocalDateTime cần format
   * @return ISO formatted string hoặc null nếu input null
   */
  public static String formatIso(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(ISO_FORMATTER);
  }

  /**
   * Format chỉ date part (yyyy-MM-dd)
   *
   * @param dateTime LocalDateTime cần format
   * @return Date string hoặc null nếu input null
   */
  public static String formatDateOnly(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DATE_ONLY_FORMATTER);
  }

  /**
   * Format chỉ time part (HH:mm:ss)
   *
   * @param dateTime LocalDateTime cần format
   * @return Time string hoặc null nếu input null
   */
  public static String formatTimeOnly(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(TIME_ONLY_FORMATTER);
  }

  /**
   * Parse string to LocalDateTime với default pattern
   *
   * @param dateTimeString String cần parse (yyyy-MM-dd HH:mm:ss)
   * @return LocalDateTime hoặc null nếu input null
   * @throws DateTimeParseException Nếu string không đúng format
   */
  public static LocalDateTime parse(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateTimeString.trim(), DEFAULT_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new DateTimeParseException(
          "Unable to parse date: " + dateTimeString, dateTimeString, 0, e);
    }
  }

  /**
   * Parse string to LocalDateTime với custom pattern
   *
   * @param dateTimeString String cần parse
   * @param pattern Pattern (ví dụ: "yyyy-MM-dd", "HH:mm:ss")
   * @return LocalDateTime hoặc null nếu input null
   * @throws DateTimeParseException Nếu string không đúng format
   */
  public static LocalDateTime parse(String dateTimeString, String pattern) {
    if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateTimeString.trim(), DateTimeFormatter.ofPattern(pattern));
    } catch (DateTimeParseException e) {
      throw new DateTimeParseException(
          "Unable to parse date with pattern '" + pattern + "': " + dateTimeString,
          dateTimeString,
          0,
          e);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid date pattern: " + pattern, e);
    }
  }

  /**
   * Parse ISO format string to LocalDateTime
   *
   * @param dateTimeString ISO format string
   * @return LocalDateTime hoặc null nếu input null
   * @throws DateTimeParseException Nếu string không đúng format
   */
  public static LocalDateTime parseIso(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(dateTimeString.trim(), ISO_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new DateTimeParseException(
          "Unable to parse ISO date: " + dateTimeString, dateTimeString, 0, e);
    }
  }

  /**
   * Convert LocalDateTime to ZonedDateTime với timezone
   *
   * @param dateTime LocalDateTime
   * @param zoneId ZoneId (ví dụ: "Asia/Ho_Chi_Minh", "UTC")
   * @return ZonedDateTime hoặc null nếu input null
   */
  public static ZonedDateTime toZonedDateTime(LocalDateTime dateTime, String zoneId) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.atZone(ZoneId.of(zoneId));
  }

  /**
   * Convert LocalDateTime to ZonedDateTime với UTC timezone
   *
   * @param dateTime LocalDateTime
   * @return ZonedDateTime hoặc null nếu input null
   */
  public static ZonedDateTime toUtc(LocalDateTime dateTime) {
    return toZonedDateTime(dateTime, "UTC");
  }

  /**
   * Convert ZonedDateTime to LocalDateTime (lose timezone info)
   *
   * @param zonedDateTime ZonedDateTime
   * @return LocalDateTime hoặc null nếu input null
   */
  public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
    if (zonedDateTime == null) {
      return null;
    }
    return zonedDateTime.toLocalDateTime();
  }

  /**
   * Add days to LocalDateTime
   *
   * @param dateTime LocalDateTime
   * @param days Số ngày cần thêm (có thể âm để trừ)
   * @return LocalDateTime sau khi thêm days
   */
  public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.plusDays(days);
  }

  /**
   * Add hours to LocalDateTime
   *
   * @param dateTime LocalDateTime
   * @param hours Số giờ cần thêm (có thể âm để trừ)
   * @return LocalDateTime sau khi thêm hours
   */
  public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.plusHours(hours);
  }

  /**
   * Add months to LocalDateTime
   *
   * @param dateTime LocalDateTime
   * @param months Số tháng cần thêm (có thể âm để trừ)
   * @return LocalDateTime sau khi thêm months
   */
  public static LocalDateTime addMonths(LocalDateTime dateTime, long months) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.plusMonths(months);
  }

  /**
   * Add years to LocalDateTime
   *
   * @param dateTime LocalDateTime
   * @param years Số năm cần thêm (có thể âm để trừ)
   * @return LocalDateTime sau khi thêm years
   */
  public static LocalDateTime addYears(LocalDateTime dateTime, long years) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.plusYears(years);
  }

  /**
   * Calculate difference in days between two LocalDateTime
   *
   * @param start Start date
   * @param end End date
   * @return Số ngày chênh lệch (có thể âm nếu end < start)
   */
  public static long daysBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }
    return ChronoUnit.DAYS.between(start, end);
  }

  /**
   * Calculate difference in hours between two LocalDateTime
   *
   * @param start Start date
   * @param end End date
   * @return Số giờ chênh lệch (có thể âm nếu end < start)
   */
  public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }
    return ChronoUnit.HOURS.between(start, end);
  }

  /**
   * Check nếu dateTime đã qua (trong quá khứ)
   *
   * @param dateTime LocalDateTime cần check
   * @return true nếu dateTime < now
   */
  public static boolean isPast(LocalDateTime dateTime) {
    if (dateTime == null) {
      return false;
    }
    return dateTime.isBefore(LocalDateTime.now());
  }

  /**
   * Check nếu dateTime chưa đến (trong tương lai)
   *
   * @param dateTime LocalDateTime cần check
   * @return true nếu dateTime > now
   */
  public static boolean isFuture(LocalDateTime dateTime) {
    if (dateTime == null) {
      return false;
    }
    return dateTime.isAfter(LocalDateTime.now());
  }

  /**
   * Format relative time (ví dụ: "2 days ago", "in 3 hours")
   *
   * @param dateTime LocalDateTime
   * @return Relative time string
   */
  public static String formatRelative(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "unknown";
    }

    LocalDateTime now = LocalDateTime.now();
    long days = daysBetween(dateTime, now);
    long hours = hoursBetween(dateTime, now);

    if (days == 0) {
      if (hours == 0) {
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes == 0) {
          return "just now";
        }
        return Math.abs(minutes) + " minute" + (Math.abs(minutes) != 1 ? "s" : "") + 
               (minutes < 0 ? " ago" : " from now");
      }
      return Math.abs(hours) + " hour" + (Math.abs(hours) != 1 ? "s" : "") + 
             (hours < 0 ? " ago" : " from now");
    }

    if (Math.abs(days) < 7) {
      return Math.abs(days) + " day" + (Math.abs(days) != 1 ? "s" : "") + 
             (days < 0 ? " ago" : " from now");
    }

    if (Math.abs(days) < 30) {
      long weeks = days / 7;
      return Math.abs(weeks) + " week" + (Math.abs(weeks) != 1 ? "s" : "") + 
             (days < 0 ? " ago" : " from now");
    }

    if (Math.abs(days) < 365) {
      long months = days / 30;
      return Math.abs(months) + " month" + (Math.abs(months) != 1 ? "s" : "") + 
             (days < 0 ? " ago" : " from now");
    }

    long years = days / 365;
    return Math.abs(years) + " year" + (Math.abs(years) != 1 ? "s" : "") + 
           (days < 0 ? " ago" : " from now");
  }
}
