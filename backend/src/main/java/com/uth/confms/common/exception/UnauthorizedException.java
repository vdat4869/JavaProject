package com.uth.confms.common.exception;

public class UnauthorizedException extends BusinessException {
  public UnauthorizedException(String message) {
    super(message, "UNAUTHORIZED");
  }

  public UnauthorizedException() {
    super("Unauthorized access", "UNAUTHORIZED");
  }
}
