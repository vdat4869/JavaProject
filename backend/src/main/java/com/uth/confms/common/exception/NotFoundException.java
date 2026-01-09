package com.uth.confms.common.exception;

public class NotFoundException extends BusinessException {
  public NotFoundException(String resource, Long id) {
    super(String.format("%s with id %d not found", resource, id), "NOT_FOUND");
  }

  public NotFoundException(String message) {
    super(message, "NOT_FOUND");
  }
}
