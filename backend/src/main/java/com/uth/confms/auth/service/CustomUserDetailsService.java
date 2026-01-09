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

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPassword())
        .authorities(getAuthorities(user))
        .accountExpired(false)
        .accountLocked(!user.getActive())
        .credentialsExpired(false)
        .disabled(!user.getActive())
        .build();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(User user) {
    return user.getRoles().stream()
        .flatMap(
            role ->
                role.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName())))
        .collect(Collectors.toList());
  }
}
