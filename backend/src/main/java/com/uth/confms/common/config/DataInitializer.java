package com.uth.confms.common.config;

import com.uth.confms.auth.entity.Permission;
import com.uth.confms.auth.entity.Role;
import com.uth.confms.auth.enums.RoleName;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.PermissionRepository;
import com.uth.confms.auth.repository.RoleRepository;
import com.uth.confms.auth.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
      RoleRepository roleRepository,
      PermissionRepository permissionRepository,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.roleRepository = roleRepository;
    this.permissionRepository = permissionRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    initializeRolesAndPermissions();
    initializeAdminUser();
  }

  private void initializeRolesAndPermissions() {
    // Create permissions
    List<String> permissionNames = Arrays.asList(
        "conference:create",
        "conference:read",
        "conference:update",
        "conference:delete",
        "submission:create",
        "submission:read",
        "submission:update",
        "submission:delete",
        "review:create",
        "review:read",
        "review:update",
        "decision:create",
        "decision:read",
        "pc:manage",
        "pc:invite");

    for (String permName : permissionNames) {
      if (permissionRepository.findByName(permName).isEmpty()) {
        String[] parts = permName.split(":");
        Permission perm = Permission.builder()
            .name(permName)
            .description("Permission: " + permName)
            .resource(parts.length > 0 ? parts[0] : null)
            .action(parts.length > 1 ? parts[1] : null)
            .build();
        permissionRepository.save(perm);
      }
    }

    // Create roles and assign permissions
    for (RoleName roleName : RoleName.values()) {
      Role role = roleRepository.findByName(roleName).orElse(null);
      if (role == null) {
        role = Role.builder().name(roleName).description("Role: " + roleName).build();
        role = roleRepository.save(role);
      }

      // Assign permissions to roles
      assignPermissionsToRole(role, roleName);
    }
  }

  private void assignPermissionsToRole(Role role, RoleName roleName) {
    List<Permission> permissionsToAssign = new java.util.ArrayList<>();

    switch (roleName) {
      case ADMIN:
        // ADMIN has all permissions
        permissionsToAssign.addAll(permissionRepository.findAll());
        break;

      case CHAIR:
        // CHAIR can manage conferences, submissions, decisions, and PC
        permissionsToAssign.addAll(
            permissionRepository.findAll().stream()
                .filter(
                    p -> p.getName().startsWith("conference:")
                        || p.getName().startsWith("submission:")
                        || p.getName().startsWith("decision:")
                        || p.getName().startsWith("pc:"))
                .toList());
        break;

      case PC:
        // PC can read conferences, manage submissions, create/read reviews, and manage
        // PC
        permissionsToAssign.addAll(
            permissionRepository.findAll().stream()
                .filter(
                    p -> p.getName().equals("conference:read")
                        || p.getName().startsWith("submission:")
                        || p.getName().startsWith("review:")
                        || p.getName().equals("pc:manage"))
                .toList());
        break;

      case AUTHOR:
        // AUTHOR can read conferences and manage their own submissions
        permissionsToAssign.addAll(
            permissionRepository.findAll().stream()
                .filter(
                    p -> p.getName().equals("conference:read")
                        || p.getName().startsWith("submission:"))
                .toList());
        break;
    }

    // Add permissions to role if not already present
    boolean updated = false;
    for (Permission perm : permissionsToAssign) {
      if (!role.getPermissions().contains(perm)) {
        role.getPermissions().add(perm);
        updated = true;
      }
    }

    if (updated) {
      roleRepository.save(role);
    }
  }

  private void initializeAdminUser() {
    // Check if admin user already exists
    if (userRepository.findByEmail("admin@uth.edu.vn").isPresent()) {
      return;
    }

    // Create admin user
    User adminUser = User.builder()
        .email("admin@uth.edu.vn")
        .password(passwordEncoder.encode("admin123"))
        .firstName("Admin")
        .lastName("System")
        .emailVerified(true) // Admin email is pre-verified
        .active(true)
        .build();

    // Assign ADMIN role
    Role adminRole = roleRepository
        .findByName(RoleName.ADMIN)
        .orElseThrow(
            () -> new RuntimeException("ADMIN role not found. Please run initializeRolesAndPermissions first."));

    adminUser.getRoles().add(adminRole);
    userRepository.save(adminUser);
  }
}
