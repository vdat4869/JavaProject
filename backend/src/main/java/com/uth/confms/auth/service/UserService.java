package com.uth.confms.auth.service;

import com.uth.confms.auth.dto.UserDTO;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý các logic liên quan đến User
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class UserService {
  private final UserRepository userRepository;
  private final com.uth.confms.common.repository.OrganizationRepository organizationRepository;

  public UserService(UserRepository userRepository,
      com.uth.confms.common.repository.OrganizationRepository organizationRepository) {
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
  }

  /**
   * Lấy thông tin user theo ID
   *
   * @param id ID của user
   * @return UserDTO chứa thông tin user
   * @throws NotFoundException nếu user không tồn tại
   */
  public UserDTO getUserById(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    return mapToDTO(user);
  }

  /**
   * Lấy ID user theo email
   *
   * @param email Email của user
   * @return ID của user
   * @throws NotFoundException nếu user không tồn tại
   */
  public Long getUserIdByEmail(String email) {
    User user = userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    return user.getId();
  }

  /**
   * Lấy thông tin user theo email
   *
   * @param email Email của user
   * @return UserDTO chứa thông tin user
   * @throws NotFoundException nếu user không tồn tại
   */
  public UserDTO getUserByEmail(String email) {
    User user = userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found"));
    return mapToDTO(user);
  }

  /**
   * Cập nhật thông tin profile user
   *
   * @param userId  ID của user cần cập nhật
   * @param userDTO Thông tin mới của user
   * @return UserDTO chứa thông tin user sau khi cập nhật
   * @throws NotFoundException nếu user không tồn tại
   */
  @Transactional
  public UserDTO updateUser(Long userId, UserDTO userDTO) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));

    if (userDTO.getFirstName() != null) {
      user.setFirstName(userDTO.getFirstName());
    }
    if (userDTO.getLastName() != null) {
      user.setLastName(userDTO.getLastName());
    }
    if (userDTO.getOrganizationId() != null) {
      com.uth.confms.common.entity.Organization org = organizationRepository.findById(userDTO.getOrganizationId())
          .orElseThrow(() -> new NotFoundException("Organization", userDTO.getOrganizationId()));
      user.setOrganization(org);
    }
    if (userDTO.getPhone() != null) {
      user.setPhone(userDTO.getPhone());
    }

    user = userRepository.save(user);
    return mapToDTO(user);
  }

  /**
   * Lấy danh sách tất cả user đang active
   *
   * @return List chứa UserDTO của tất cả active users
   */
  public List<UserDTO> getAllActiveUsers() {
    return userRepository.findByActiveTrue().stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Lấy danh sách user đang active (có phân trang)
   *
   * @param pageable Thông tin phân trang
   * @return Page chứa UserDTO của active users
   */
  public Page<UserDTO> getAllActiveUsers(Pageable pageable) {
    Page<User> users = userRepository.findByActiveTrue(pageable);
    return new PageImpl<>(
        users.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList()),
        pageable,
        users.getTotalElements());
  }

  /**
   * Lấy tất cả user (có phân trang)
   *
   * @param pageable Thông tin phân trang
   * @return Page chứa UserDTO của tất cả users
   */
  public Page<UserDTO> getAllUsers(Pageable pageable) {
    Page<User> users = userRepository.findAll(pageable);
    return new PageImpl<>(
        users.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList()),
        pageable,
        users.getTotalElements());
  }

  /**
   * Tìm kiếm user theo tên hoặc email
   *
   * @param keyword Từ khóa tìm kiếm
   * @return List chứa UserDTO matching criteria
   */
  public List<UserDTO> searchUsers(String keyword) {
    return userRepository.searchByNameOrEmail(keyword).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  /**
   * Tìm kiếm user theo tên hoặc email (có phân trang)
   *
   * @param keyword  Từ khóa tìm kiếm
   * @param pageable Thông tin phân trang
   * @return Page chứa UserDTO matching criteria
   */
  public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
    Page<User> users = userRepository.searchByNameOrEmail(keyword, pageable);
    return new PageImpl<>(
        users.getContent().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList()),
        pageable,
        users.getTotalElements());
  }

  /**
   * Deactivate user account
   *
   * @param userId ID của user cần deactivate
   * @throws NotFoundException nếu user không tồn tại
   */
  @Transactional
  public void deactivateUser(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
    user.setActive(false);
    userRepository.save(user);
  }

  /**
   * Activate user account
   *
   * @param userId ID của user cần activate
   * @throws NotFoundException nếu user không tồn tại
   */
  @Transactional
  public void activateUser(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
    user.setActive(true);
    userRepository.save(user);
  }

  /**
   * Đếm tổng số user đang active
   *
   * @return Số lượng active users
   */
  public long countActiveUsers() {
    return userRepository.countByActiveTrue();
  }

  /**
   * Đếm tổng số user đã verify email
   *
   * @return Số lượng users with verified emails
   */
  public long countVerifiedUsers() {
    return userRepository.countByEmailVerifiedTrue();
  }

  /**
   * Map User entity to UserDTO
   *
   * @param user User entity
   * @return UserDTO
   */
  private UserDTO mapToDTO(User user) {
    Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());

    return UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
        .organizationName(user.getOrganization() != null ? user.getOrganization().getName() : null)
        .phone(user.getPhone())
        .emailVerified(user.getEmailVerified())
        .active(user.getActive())
        .roles(roles)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
