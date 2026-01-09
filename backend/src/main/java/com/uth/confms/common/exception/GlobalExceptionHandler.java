package com.uth.confms.common.exception;

import com.uth.confms.common.dto.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.jsonwebtoken.JwtException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
    log.warn("Business exception: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NotFoundException e) {
    log.warn("Not found: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(UnauthorizedException e) {
    log.warn("Unauthorized: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
      BadCredentialsException e) {
    log.warn("Bad credentials: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Invalid email or password"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
    log.warn("Access denied: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"));
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException e) {
    log.warn("JWT error: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("Invalid or expired token"));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    // Check if it's a JWT-related error
    String message = e.getMessage();
    if (message != null && (message.contains("JWT") || message.contains("token") || message.contains("Token"))) {
      log.warn("JWT validation error: {}", message);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error("Invalid token format"));
    }
    log.warn("Illegal argument: {}", message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message != null ? message : "Invalid argument"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
      MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    log.warn("Validation errors: {}", errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("Validation failed", errors));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
    log.error("Unexpected error: ", e);
    // In development, return detailed error message
    String errorMessage = "An unexpected error occurred";
    if (e.getMessage() != null) {
      errorMessage = e.getMessage();
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(errorMessage));
  }
}
