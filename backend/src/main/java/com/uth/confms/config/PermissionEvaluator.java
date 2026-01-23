package com.uth.confms.config;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom PermissionEvaluator để check permissions trong @PreAuthorize
 *
 * <p>Usage:
 * <pre>
 * @PreAuthorize("hasPermission(#id, 'Conference', 'UPDATE')")
 * </pre>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Component
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

  private final UserRepository userRepository;

  public PermissionEvaluator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean hasPermission(
      Authentication authentication, Object targetDomainObject, Object permission) {
    if (authentication == null || targetDomainObject == null || permission == null) {
      return false;
    }

    String username = authentication.getName();
    String permissionString = permission.toString().toUpperCase();

    // Get user and check permissions
    User user =
        userRepository
            .findByEmail(username)
            .orElse(null);

    if (user == null) {
      return false;
    }

    // Check if user has the required permission
    return user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .anyMatch(
            perm -> {
              // Check exact permission name
              if (perm.getName().equalsIgnoreCase(permissionString)) {
                return true;
              }
              // Check resource:action format (e.g., "conference:update")
              String resourceAction = targetDomainObject.toString().toLowerCase() + ":" + permissionString.toLowerCase();
              return perm.getName().equalsIgnoreCase(resourceAction);
            });
  }

  @Override
  public boolean hasPermission(
      Authentication authentication, Serializable targetId, String targetType, Object permission) {
    if (authentication == null || targetType == null || permission == null) {
      return false;
    }

    String username = authentication.getName();
    String permissionString = permission.toString().toUpperCase();

    // Get user and check permissions
    User user =
        userRepository
            .findByEmail(username)
            .orElse(null);

    if (user == null) {
      return false;
    }

    // Check if user has the required permission
    String resourceAction = targetType.toLowerCase() + ":" + permissionString.toLowerCase();
    return user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .anyMatch(perm -> perm.getName().equalsIgnoreCase(resourceAction));
  }
}
