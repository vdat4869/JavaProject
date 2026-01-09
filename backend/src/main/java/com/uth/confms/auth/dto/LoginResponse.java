package com.uth.confms.auth.dto;

import java.util.Set;

public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long userId;
  private String email;
  private String fullName;
  private Set<String> roles;
  private Boolean emailVerified;

  public LoginResponse() {}

  public LoginResponse(
      String accessToken,
      String refreshToken,
      String tokenType,
      Long userId,
      String email,
      String fullName,
      Set<String> roles,
      Boolean emailVerified) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenType = tokenType;
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
    this.roles = roles;
    this.emailVerified = emailVerified;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public Boolean getEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(Boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    private Set<String> roles;
    private Boolean emailVerified;

    public Builder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public Builder refreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public Builder tokenType(String tokenType) {
      this.tokenType = tokenType;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder fullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public Builder roles(Set<String> roles) {
      this.roles = roles;
      return this;
    }

    public Builder emailVerified(Boolean emailVerified) {
      this.emailVerified = emailVerified;
      return this;
    }

    public LoginResponse build() {
      return new LoginResponse(
          accessToken, refreshToken, tokenType, userId, email, fullName, roles, emailVerified);
    }
  }
}
