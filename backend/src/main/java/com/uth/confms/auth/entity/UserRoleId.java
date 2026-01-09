package com.uth.confms.auth.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserRoleId implements Serializable {
  private Long userId;
  private Long roleId;

  public UserRoleId() {}

  public UserRoleId(Long userId, Long roleId) {
    this.userId = userId;
    this.roleId = roleId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserRoleId that = (UserRoleId) o;
    return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, roleId);
  }
}
