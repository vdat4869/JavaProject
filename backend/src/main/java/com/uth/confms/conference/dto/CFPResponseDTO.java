package com.uth.confms.conference.dto;

import java.time.LocalDateTime;

public class CFPResponseDTO {
  private Long id;
  private String callForPapers;
  private String topics;
  private String submissionGuidelines;
  private Boolean open;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public CFPResponseDTO() {}

  public CFPResponseDTO(
      Long id,
      String callForPapers,
      String topics,
      String submissionGuidelines,
      Boolean open,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.callForPapers = callForPapers;
    this.topics = topics;
    this.submissionGuidelines = submissionGuidelines;
    this.open = open;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCallForPapers() {
    return callForPapers;
  }

  public void setCallForPapers(String callForPapers) {
    this.callForPapers = callForPapers;
  }

  public String getTopics() {
    return topics;
  }

  public void setTopics(String topics) {
    this.topics = topics;
  }

  public String getSubmissionGuidelines() {
    return submissionGuidelines;
  }

  public void setSubmissionGuidelines(String submissionGuidelines) {
    this.submissionGuidelines = submissionGuidelines;
  }

  public Boolean getOpen() {
    return open;
  }

  public void setOpen(Boolean open) {
    this.open = open;
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

  public static class Builder {
    private Long id;
    private String callForPapers;
    private String topics;
    private String submissionGuidelines;
    private Boolean open;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder callForPapers(String callForPapers) {
      this.callForPapers = callForPapers;
      return this;
    }

    public Builder topics(String topics) {
      this.topics = topics;
      return this;
    }

    public Builder submissionGuidelines(String submissionGuidelines) {
      this.submissionGuidelines = submissionGuidelines;
      return this;
    }

    public Builder open(Boolean open) {
      this.open = open;
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

    public CFPResponseDTO build() {
      return new CFPResponseDTO(id, callForPapers, topics, submissionGuidelines, open, createdAt, updatedAt);
    }
  }
}
