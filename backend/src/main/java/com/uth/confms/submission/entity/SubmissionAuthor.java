package com.uth.confms.submission.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_authors")
public class SubmissionAuthor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "submission_id", nullable = false)
  private Submission submission;

  @Column(nullable = true)
  private Long userId; // ID hệ thống (nếu có)

  @Column(nullable = false)
  private String firstName; // Tên

  @Column(nullable = false)
  private String lastName; // Họ

  private String email;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id")
  private com.uth.confms.common.entity.Organization organization;

  private String affiliation; // Đơn vị công tác (nếu không chọn organization)

  @Column(nullable = false)
  private Boolean isCorresponding = false; // Tác giả liên hệ chính?

  @Column(nullable = false)
  private Integer orderIndex = 0; // Thứ tự tác giả

  public SubmissionAuthor() {
    this.isCorresponding = false;
    this.orderIndex = 0;
  }

  public SubmissionAuthor(
      Long id,
      Submission submission,
      Long userId,
      String firstName,
      String lastName,
      String email,
      String affiliation,
      Boolean isCorresponding,
      Integer orderIndex) {
    this.id = id;
    this.submission = submission;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.affiliation = affiliation;
    this.isCorresponding = isCorresponding != null ? isCorresponding : false;
    this.orderIndex = orderIndex != null ? orderIndex : 0;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Submission getSubmission() {
    return submission;
  }

  public void setSubmission(Submission submission) {
    this.submission = submission;
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

  public com.uth.confms.common.entity.Organization getOrganization() {
    return organization;
  }

  public void setOrganization(com.uth.confms.common.entity.Organization organization) {
    this.organization = organization;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Submission submission;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String affiliation;
    private Boolean isCorresponding = false;
    private Integer orderIndex = 0;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder submission(Submission submission) {
      this.submission = submission;
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
      this.isCorresponding = isCorresponding != null ? isCorresponding : false;
      return this;
    }

    public Builder orderIndex(Integer orderIndex) {
      this.orderIndex = orderIndex != null ? orderIndex : 0;
      return this;
    }

    public SubmissionAuthor build() {
      return new SubmissionAuthor(
          id, submission, userId, firstName, lastName, email, affiliation, isCorresponding,
          orderIndex);
    }
  }
}
