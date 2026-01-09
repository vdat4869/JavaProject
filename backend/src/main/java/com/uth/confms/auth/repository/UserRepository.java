package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho User entity
 *
 * <p>Repository này cung cấp các methods để truy vấn User:
 *
 * <ul>
 *   <li>findByEmail - Tìm user theo email
 *   <li>existsByEmail - Kiểm tra email đã tồn tại chưa
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
}
