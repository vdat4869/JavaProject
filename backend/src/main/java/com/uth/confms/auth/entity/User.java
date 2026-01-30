package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.uth.confms.auth.enums.LoginProvider;

/**
 * Entity đại diện cho người dùng trong hệ thống
 *
 * User có thể có nhiều roles (ADMIN, CHAIR, PC, REVIEWER, AUTHOR). User phải
 * verify email trước khi có thể tham gia các workflow của hệ thống.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = true)
  private String password;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id")
  private com.uth.confms.common.entity.Organization organization; // Tổ chức/Trường đại học

  private String phone; // Số điện thoại

  @Builder.Default
  @Column(nullable = false)
  private Boolean emailVerified = true; // Trạng thái xác thực email (mặc định là true hệ thống hiện tại)

  @Builder.Default
  @Column(nullable = false)
  private Boolean active = true; // Trạng thái hoạt động của tài khoản

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  @Builder.Default
  private LoginProvider provider = LoginProvider.LOCAL;

  /**
   * ID user từ Google (sub trong id_token)
   */
  @Column(unique = true)
  private String providerId;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt; // Thời điểm tạo tài khoản

  @LastModifiedDate
  private LocalDateTime updatedAt; // Thời điểm cập nhật cuối cùng

  /**
   * Lấy tên đầy đủ của user (firstName + lastName)
   *
   * @return Tên đầy đủ của user
   */
  public String getFullName() {
    return firstName + " " + lastName;
  }
}
