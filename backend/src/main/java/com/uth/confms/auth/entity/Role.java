package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  @Enumerated(EnumType.STRING)
  private RoleName name;

  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions = new HashSet<>();

  public Role() {
    this.permissions = new HashSet<>();
  }

  public Role(Long id, RoleName name, String description, Set<Permission> permissions) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.permissions = permissions != null ? permissions : new HashSet<>();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RoleName getName() {
    return name;
  }

  public void setName(RoleName name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<Permission> permissions) {
    this.permissions = permissions != null ? permissions : new HashSet<>();
  }

  public enum RoleName {
    ADMIN,
    CHAIR,
    PC,
    REVIEWER,
    AUTHOR
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private RoleName name;
    private String description;
    private Set<Permission> permissions = new HashSet<>();

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(RoleName name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder permissions(Set<Permission> permissions) {
      this.permissions = permissions != null ? permissions : new HashSet<>();
      return this;
    }

    public Role build() {
      return new Role(id, name, description, permissions);
    }
  }
}
