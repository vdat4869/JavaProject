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
        Permission perm = Permission.builder().name(permName).description("Permission: " + permName).build();
        permissionRepository.save(perm);
      }
    }

    // Create roles
    for (RoleName roleName : RoleName.values()) {
      if (roleRepository.findByName(roleName).isEmpty()) {
        Role role = Role.builder().name(roleName).description("Role: " + roleName).build();
        roleRepository.save(role);
      }
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
        .affiliation("UTH University")
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
