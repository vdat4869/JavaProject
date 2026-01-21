package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.enums.LoginProvider;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho User entity
 *
 * <p>
 * Repository này cung cấp các methods để truy vấn User:
 *
 * <ul>
 * <li>findByEmail - Tìm user theo email
 * <li>existsByEmail - Kiểm tra email đã tồn tại chưa
 * <li>findAllActive - Lấy danh sách tất cả user active
 * <li>searchByNameOrEmail - Tìm kiếm user theo tên hoặc email
 * <li>findByProvider - Tìm user theo login provider
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * Tìm user theo email
   *
   * @param email Email của user
   * @return Optional chứa User nếu tìm thấy
   */
  Optional<User> findByEmail(String email);

  /**
   * Kiểm tra email đã tồn tại trong hệ thống chưa
   *
   * @param email Email cần kiểm tra
   * @return true nếu email đã tồn tại, false nếu chưa
   */
  boolean existsByEmail(String email);

  /**
   * Lấy danh sách tất cả user đang active
   *
   * @return List chứa tất cả active users
   */
  List<User> findByActiveTrue();

  /**
   * Lấy danh sách user đang active (có phân trang)
   *
   * @param pageable Thông tin phân trang
   * @return Page chứa active users
   */
  Page<User> findByActiveTrue(Pageable pageable);

  /**
   * Tìm kiếm user theo tên hoặc email (case-insensitive)
   *
   * @param keyword Từ khóa tìm kiếm
   * @return List chứa users matching criteria
   */
  @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<User> searchByNameOrEmail(@Param("keyword") String keyword);

  /**
   * Tìm kiếm user theo tên hoặc email (có phân trang)
   *
   * @param keyword  Từ khóa tìm kiếm
   * @param pageable Thông tin phân trang
   * @return Page chứa users matching criteria
   */
  @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<User> searchByNameOrEmail(@Param("keyword") String keyword, Pageable pageable);

  /**
   * Tìm user theo login provider
   *
   * @param provider Login provider
   * @return List chứa users của provider đó
   */
  List<User> findByProvider(LoginProvider provider);

  /**
   * Tìm user theo providerId (Google ID, Facebook ID, etc.)
   *
   * @param providerId ID từ external provider
   * @return Optional chứa User nếu tìm thấy
   */
  Optional<User> findByProviderId(String providerId);

  /**
   * Đếm số user đang active
   *
   * @return Số lượng active users
   */
  long countByActiveTrue();

  /**
   * Đếm số user đã verify email
   *
   * @return Số lượng users with verified emails
   */
  long countByEmailVerifiedTrue();
}
