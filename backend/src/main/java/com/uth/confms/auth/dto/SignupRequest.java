package com.uth.confms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho request đăng ký tài khoản mới
 *
 * <p>DTO này chứa thông tin cần thiết để tạo user mới. Sau khi đăng ký, user sẽ nhận email
 * verification.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class SignupRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  private String affiliation;

  private String phone;

  public SignupRequest() {}

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
