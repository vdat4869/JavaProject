package com.uth.confms.submission.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmissionAuthorDTO {
  private Long id;
  private Long userId;

  @NotBlank(message = "First name is required")
  private String firstName; // Tên

  @NotBlank(message = "Last name is required")
  private String lastName; // Họ

  private String email;
  private String affiliation; // Đơn vị công tác
  private Boolean isCorresponding; // Tác giả liên hệ chính?
  private Integer orderIndex; // Thứ tự

  public SubmissionAuthorDTO() {
  }

  public SubmissionAuthorDTO(
      Long id,
      Long userId,
      String firstName,
      String lastName,
      String email,
      String affiliation,
      Boolean isCorresponding,
      Integer orderIndex) {
    this.id = id;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.affiliation = affiliation;
    this.isCorresponding = isCorresponding;
    this.orderIndex = orderIndex;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAffiliation() {
    return affiliation;
  }

  public void setAffiliation(String affiliation) {
    this.affiliation = affiliation;
  }

  public Boolean getIsCorresponding() {
    return isCorresponding;
  }

  public void setIsCorresponding(Boolean isCorresponding) {
    this.isCorresponding = isCorresponding;
  }

  public Integer getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(Integer orderIndex) {
    this.orderIndex = orderIndex;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String affiliation;
    private Boolean isCorresponding;
    private Integer orderIndex;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
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

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder affiliation(String affiliation) {
      this.affiliation = affiliation;
      return this;
    }

    public Builder isCorresponding(Boolean isCorresponding) {
      this.isCorresponding = isCorresponding;
      return this;
    }

    public Builder orderIndex(Integer orderIndex) {
      this.orderIndex = orderIndex;
      return this;
    }

    public SubmissionAuthorDTO build() {
      return new SubmissionAuthorDTO(
          id, userId, firstName, lastName, email, affiliation, isCorresponding, orderIndex);
    }
  }
}
