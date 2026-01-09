package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.UserDTO;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserDTO getUserById(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    return mapToDTO(user);
  }

  public Long getUserIdByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
    return user.getId();
  }

  @Transactional
  public UserDTO updateUser(Long userId, UserDTO userDTO) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));

    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setAffiliation(userDTO.getAffiliation());
    user.setPhone(userDTO.getPhone());

    user = userRepository.save(user);
    return mapToDTO(user);
  }

  private UserDTO mapToDTO(User user) {
    Set<String> roles =
        user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());

    return UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .affiliation(user.getAffiliation())
        .phone(user.getPhone())
        .emailVerified(user.getEmailVerified())
        .active(user.getActive())
        .roles(roles)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
