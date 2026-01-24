package com.uth.confms.auth.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

    // Đảm bảo password không null (cho trường hợp Google SSO)
    String password = user.getPassword();
    if (password == null) {
      // Nếu password null, dùng một giá trị mặc định (sẽ không bao giờ match)
      // Vì user này chỉ login qua SSO, không dùng password authentication
      password = "{noop}GOOGLE_SSO_NO_PASSWORD";
    }

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(password)
        .authorities(getAuthorities(user))
        .accountExpired(false)
        .accountLocked(!user.getActive())
        .credentialsExpired(false)
        .disabled(!user.getActive())
        .build();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(User user) {
    java.util.Set<GrantedAuthority> authorities = new java.util.HashSet<>();

    // Add roles as authorities (for @PreAuthorize hasRole())
    user.getRoles().forEach(
        role -> {
          // Add role without ROLE_ prefix (MethodSecurityConfig sets defaultRolePrefix to "")
          authorities.add(new SimpleGrantedAuthority(role.getName().name()));
        });

    // Add permissions as authorities (for @PreAuthorize hasAuthority())
    user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .forEach(
            permission -> {
              authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });

    return authorities;
  }
}
