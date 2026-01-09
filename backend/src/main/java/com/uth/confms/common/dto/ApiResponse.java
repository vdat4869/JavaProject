package com.uth.confms.common.dto;

public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;

  public ApiResponse() {}

  public ApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  // Getters and Setters
  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  /**
   * Tạo success response với data
   *
   * @param <T> Type của data
   * @param data Dữ liệu trả về
   * @return ApiResponse với success = true và message = "Success"
   */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().success(true).message("Success").data(data).build();
  }

  /**
   * Tạo success response với message và data
   *
   * @param <T> Type của data
   * @param message Thông báo success
   * @param data Dữ liệu trả về
   * @return ApiResponse với success = true
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder().success(true).message(message).data(data).build();
  }

  /**
   * Tạo error response
   *
   * @param <T> Type của data
   * @param message Thông báo lỗi
   * @return ApiResponse với success = false
   */
  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder().success(false).message(message).build();
  }

  /**
   * Tạo error response với data
   *
   * @param <T> Type của data
   * @param message Thông báo lỗi
   * @param data Dữ liệu trả về (có thể là error details)
   * @return ApiResponse với success = false
   */
  public static <T> ApiResponse<T> error(String message, T data) {
    return ApiResponse.<T>builder().success(false).message(message).data(data).build();
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static class Builder<T> {
    private boolean success;
    private String message;
    private T data;

    public Builder<T> success(boolean success) {
      this.success = success;
      return this;
    }

    public Builder<T> message(String message) {
      this.message = message;
      return this;
    }

    public Builder<T> data(T data) {
      this.data = data;
      return this;
    }

    public ApiResponse<T> build() {
      return new ApiResponse<>(success, message, data);
    }
  }
}
