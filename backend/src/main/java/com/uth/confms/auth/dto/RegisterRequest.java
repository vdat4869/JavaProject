package com.uth.confms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
  @NotBlank @Email private String email;

  @NotBlank
  @Size(min = 8)
  private String password;

  @NotBlank private String firstName;

  @NotBlank private String lastName;

  private String affiliation;

  private String phone;

  public RegisterRequest() {}

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
}
