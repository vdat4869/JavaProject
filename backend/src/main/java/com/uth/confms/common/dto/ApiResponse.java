package com.uth.confms.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API Response wrapper
 *
 * <p>
 * Response format cho tất cả API endpoints:
 * 
 * <pre>
 * {
 *   "success": true/false,
 *   "message": "Success message or error message",
 *   "data": { ... } // Optional, excluded if null
 * }
 * </pre>
 *
 * @param <T> Type của data payload
 * @author UTH-ConfMS Team
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private boolean success; // Trạng thái thành công
  private String message; // Thông báo
  private T data; // Dữ liệu trả về

  /**
   * Tạo success response với data
   *
   * @param <T>  Type của data
   * @param data Dữ liệu trả về
   * @return ApiResponse với success = true và message = "Success"
   */
  // Tạo response thành công
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message("Success")
        .data(data)
        .build();
  }

  /**
   * Tạo success response với message và data
   *
   * @param <T>     Type của data
   * @param message Thông báo success
   * @param data    Dữ liệu trả về
   * @return ApiResponse với success = true
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .build();
  }

  /**
   * Tạo error response
   *
   * @param <T>     Type của data
   * @param message Thông báo lỗi
   * @return ApiResponse với success = false
   */
  // Tạo response lỗi
  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .build();
  }

  /**
   * Tạo error response với data (ví dụ: validation errors)
   *
   * @param <T>     Type của data
   * @param message Thông báo lỗi
   * @param data    Dữ liệu trả về (có thể là error details)
   * @return ApiResponse với success = false
   */
  public static <T> ApiResponse<T> error(String message, T data) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .data(data)
        .build();
  }
}
