package com.uth.confms.auth.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserProfileDTO {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String affiliation;
  private String phone;
  private Boolean emailVerified;
  private Boolean active;
  private Set<String> roles;
  private LocalDateTime createdAt;

  public UserProfileDTO() {}

  public UserProfileDTO(
      Long id,
      String email,
      String firstName,
      String lastName,
      String affiliation,
      String phone,
      Boolean emailVerified,
      Boolean active,
      Set<String> roles,
      LocalDateTime createdAt) {
    this.id = id;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.affiliation = affiliation;
    this.phone = phone;
    this.emailVerified = emailVerified;
    this.active = active;
    this.roles = roles;
    this.createdAt = createdAt;
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

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String affiliation;
    private String phone;
    private Boolean emailVerified;
    private Boolean active;
    private Set<String> roles;
    private LocalDateTime createdAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
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
      this.emailVerified = emailVerified;
      return this;
    }

    public Builder active(Boolean active) {
      this.active = active;
      return this;
    }

    public Builder roles(Set<String> roles) {
      this.roles = roles;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public UserProfileDTO build() {
      return new UserProfileDTO(
          id,
          email,
          firstName,
          lastName,
          affiliation,
          phone,
          emailVerified,
          active,
          roles,
          createdAt);
    }
  }
}
