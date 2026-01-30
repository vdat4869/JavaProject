package com.uth.confms.conference.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CFPResponseDTO {
  private Long id;
  private String callForPapers; // Nội dung mời viết bài
  @Deprecated
  private String topics; // Đã cũ
  private List<TopicDTO> topicsList; // Danh sách chủ đề
  private List<TrackDTO> tracks; // Danh sách tracks
  private String submissionGuidelines; // Hướng dẫn nộp bài
  private Boolean open; // Trạng thái mở/đóng
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public CFPResponseDTO() {
  }

  public CFPResponseDTO(
      Long id,
      String callForPapers,
      String topics,
      List<TopicDTO> topicsList,
      List<TrackDTO> tracks,
      String submissionGuidelines,
      Boolean open,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.callForPapers = callForPapers;
    this.topics = topics;
    this.topicsList = topicsList;
    this.tracks = tracks;
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

  @Deprecated
  public String getTopics() {
    return topics;
  }

  @Deprecated
  public void setTopics(String topics) {
    this.topics = topics;
  }

  public List<TopicDTO> getTopicsList() {
    return topicsList;
  }

  public void setTopicsList(List<TopicDTO> topicsList) {
    this.topicsList = topicsList;
  }

  public List<TrackDTO> getTracks() {
    return tracks;
  }

  public void setTracks(List<TrackDTO> tracks) {
    this.tracks = tracks;
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
    @Deprecated
    private String topics;
    private List<TopicDTO> topicsList;
    private List<TrackDTO> tracks;
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

    @Deprecated
    public Builder topics(String topics) {
      this.topics = topics;
      return this;
    }

    public Builder topicsList(List<TopicDTO> topicsList) {
      this.topicsList = topicsList;
      return this;
    }

    public Builder tracks(List<TrackDTO> tracks) {
      this.tracks = tracks;
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
      return new CFPResponseDTO(id, callForPapers, topics, topicsList, tracks, submissionGuidelines, open, createdAt,
          updatedAt);
    }
  }
}
