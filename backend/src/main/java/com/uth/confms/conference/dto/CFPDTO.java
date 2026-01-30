package com.uth.confms.conference.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CFPDTO {
  @NotNull
  private Long conferenceId; // ID hội nghị liên quan

  private String callForPapers; // Nội dung CFP

  @Deprecated
  private String topics; // Đã cũ: dùng topicIds thay thế

  private List<Long> topicIds; // Danh sách ID chủ đề liên quan

  private String submissionGuidelines; // Hướng dẫn nộp bài

  private Boolean open; // Trạng thái mở/đóng nhận bài

  public CFPDTO() {
  }

  public CFPDTO(
      Long conferenceId,
      String callForPapers,
      String topics,
      List<Long> topicIds,
      String submissionGuidelines,
      Boolean open) {
    this.conferenceId = conferenceId;
    this.callForPapers = callForPapers;
    this.topics = topics;
    this.topicIds = topicIds;
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

  @Deprecated
  public String getTopics() {
    return topics;
  }

  @Deprecated
  public void setTopics(String topics) {
    this.topics = topics;
  }

  public List<Long> getTopicIds() {
    return topicIds;
  }

  public void setTopicIds(List<Long> topicIds) {
    this.topicIds = topicIds;
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
    @Deprecated
    private String topics;
    private List<Long> topicIds;
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

    @Deprecated
    public Builder topics(String topics) {
      this.topics = topics;
      return this;
    }

    public Builder topicIds(List<Long> topicIds) {
      this.topicIds = topicIds;
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
      return new CFPDTO(conferenceId, callForPapers, topics, topicIds, submissionGuidelines, open);
    }
  }
}
