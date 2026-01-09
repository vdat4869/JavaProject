package com.uth.confms.conference.dto;

import jakarta.validation.constraints.NotNull;

public class CFPDTO {
  @NotNull private Long conferenceId;

  private String callForPapers;

  private String topics;

  private String submissionGuidelines;

  private Boolean open;

  public CFPDTO() {}

  public CFPDTO(
      Long conferenceId,
      String callForPapers,
      String topics,
      String submissionGuidelines,
      Boolean open) {
    this.conferenceId = conferenceId;
    this.callForPapers = callForPapers;
    this.topics = topics;
    this.submissionGuidelines = submissionGuidelines;
    this.open = open;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
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

  public static class Builder {
    private Long conferenceId;
    private String callForPapers;
    private String topics;
    private String submissionGuidelines;
    private Boolean open;

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
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

    public CFPDTO build() {
      return new CFPDTO(conferenceId, callForPapers, topics, submissionGuidelines, open);
    }
  }
}
