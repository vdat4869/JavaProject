package com.uth.confms.auth.service;

import com.uth.confms.auth.entity.AuditLog;
import com.uth.confms.auth.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class AuditLogService {
  private final AuditLogRepository auditLogRepository;

  public AuditLogService(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAction(
      Long userId, String action, String resource, Long resourceId, String details) {
    AuditLog log = AuditLog.builder()
        .userId(userId)
        .action(action)
        .resource(resource)
        .resourceId(resourceId)
        .details(details)
        .build();
    auditLogRepository.save(log);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAction(
      Long userId,
      String username,
      String action,
      String resource,
      Long resourceId,
      String details,
      HttpServletRequest request) {
    AuditLog log = AuditLog.builder()
        .userId(userId)
        .username(username)
        .action(action)
        .resource(resource)
        .resourceId(resourceId)
        .details(details)
        .ipAddress(request != null ? getClientIpAddress(request) : null)
        .userAgent(request != null ? request.getHeader("User-Agent") : null)
        .build();
    auditLogRepository.save(log);
  }

  private String getClientIpAddress(HttpServletRequest request) {
    if (request == null)
      return null;
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
