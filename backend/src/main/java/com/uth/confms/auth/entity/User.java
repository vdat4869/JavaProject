package com.uth.confms.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho người dùng trong hệ thống
 *
 * <p>User có thể có nhiều roles (ADMIN, CHAIR, PC, REVIEWER, AUTHOR). User phải verify email trước
 * khi có thể tham gia các workflow của hệ thống.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  private String affiliation;

  private String phone;

  @Column(nullable = false)
  private Boolean emailVerified = false;

  @Column(nullable = false)
  private Boolean active = true;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  public User() {
    this.emailVerified = false;
    this.active = true;
    this.roles = new HashSet<>();
  }

  public User(
      Long id,
      String email,
      String password,
      String firstName,
      String lastName,
      String affiliation,
      String phone,
      Boolean emailVerified,
      Boolean active,
      Set<Role> roles,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.affiliation = affiliation;
    this.phone = phone;
    this.emailVerified = emailVerified != null ? emailVerified : false;
    this.active = active != null ? active : true;
    this.roles = roles != null ? roles : new HashSet<>();
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getAffiliation() {
    return affiliation;
  }

  public void setAffiliation(String affiliation) {
    this.affiliation = affiliation;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles != null ? roles : new HashSet<>();
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   * Lấy tên đầy đủ của user (firstName + lastName)
   *
   * @return Tên đầy đủ của user
   */
  public String getFullName() {
    return firstName + " " + lastName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String affiliation;
    private String phone;
    private Boolean emailVerified = false;
    private Boolean active = true;
    private Set<Role> roles = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder affiliation(String affiliation) {
      this.affiliation = affiliation;
      return this;
    }

    public Builder phone(String phone) {
      this.phone = phone;
      return this;
    }

    public Builder emailVerified(Boolean emailVerified) {
      this.emailVerified = emailVerified != null ? emailVerified : false;
      return this;
    }

    public Builder active(Boolean active) {
      this.active = active != null ? active : true;
      return this;
    }

    public Builder roles(Set<Role> roles) {
      this.roles = roles != null ? roles : new HashSet<>();
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(LocalDateTime updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public User build() {
      // Set createdAt if not already set (JPA Auditing will handle it, but this is a fallback)
      if (createdAt == null) {
        createdAt = LocalDateTime.now();
      }
      return new User(
          id,
          email,
          password,
          firstName,
          lastName,
          affiliation,
          phone,
          emailVerified,
          active,
          roles,
          createdAt,
          updatedAt);
    }
  }
}
