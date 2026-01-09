package com.uth.confms.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId.class)
public class UserRole {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @Id
  @Column(name = "role_id")
  private Long roleId;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "role_id", insertable = false, updatable = false)
  private Role role;

  public UserRole() {}

  public UserRole(Long userId, Long roleId, User user, Role role) {
    this.userId = userId;
    this.roleId = roleId;
    this.user = user;
    this.role = role;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long userId;
    private Long roleId;
    private User user;
    private Role role;

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder roleId(Long roleId) {
      this.roleId = roleId;
      return this;
    }

    public Builder user(User user) {
      this.user = user;
      return this;
    }

    public Builder role(Role role) {
      this.role = role;
      return this;
    }

    public UserRole build() {
      return new UserRole(userId, roleId, user, role);
    }
  }
}
