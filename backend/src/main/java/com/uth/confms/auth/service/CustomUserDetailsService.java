package com.uth.confms.auth.service;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import java.util.Collection;
import java.util.stream.Collectors;
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
    // Thêm roles với prefix "ROLE_" để hasRole() hoạt động
    Collection<GrantedAuthority> authorities = user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
        .collect(Collectors.toList());
    
    // Thêm permissions
    user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(permission -> new SimpleGrantedAuthority(permission.getName()))
        .forEach(authorities::add);
    
    return authorities;
  }
}
