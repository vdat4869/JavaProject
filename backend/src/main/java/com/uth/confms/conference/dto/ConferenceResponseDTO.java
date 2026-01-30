package com.uth.confms.conference.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConferenceResponseDTO {
  private Long id;
  private String name; // Tên hội nghị
  private String acronym; // Tên viết tắt
  private String description; // Mô tả
  private Long chairId; // ID của Chair
  private Boolean published; // Trạng thái công khai
  private String reviewMode; // Chế độ review
  private List<TopicDTO> topics; // Danh sách chủ đề
  private List<KeywordDTO> keywords; // Danh sách từ khóa
  private List<TrackDTO> tracks; // Danh sách tracks
  private List<DeadlineDTO> deadlines; // Danh sách mốc thời gian
  private CFPResponseDTO cfp; // Thông tin CFP
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public ConferenceResponseDTO() {
  }

  public ConferenceResponseDTO(
      Long id,
      String name,
      String acronym,
      String description,
      Long chairId,
      Boolean published,
      String reviewMode,
      List<TopicDTO> topics,
      List<KeywordDTO> keywords,
      List<TrackDTO> tracks,
      List<DeadlineDTO> deadlines,
      CFPResponseDTO cfp,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.acronym = acronym;
    this.description = description;
    this.chairId = chairId;
    this.published = published;
    this.reviewMode = reviewMode;
    this.topics = topics;
    this.keywords = keywords;
    this.tracks = tracks;
    this.deadlines = deadlines;
    this.cfp = cfp;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAcronym() {
    return acronym;
  }

  public void setAcronym(String acronym) {
    this.acronym = acronym;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getChairId() {
    return chairId;
  }

  public void setChairId(Long chairId) {
    this.chairId = chairId;
  }

  public Boolean getPublished() {
    return published;
  }

  public void setPublished(Boolean published) {
    this.published = published;
  }

  public String getReviewMode() {
    return reviewMode;
  }

  public void setReviewMode(String reviewMode) {
    this.reviewMode = reviewMode;
  }

  public List<TopicDTO> getTopics() {
    return topics;
  }

  public void setTopics(List<TopicDTO> topics) {
    this.topics = topics;
  }

  public List<KeywordDTO> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<KeywordDTO> keywords) {
    this.keywords = keywords;
  }

  public List<TrackDTO> getTracks() {
    return tracks;
  }

  public void setTracks(List<TrackDTO> tracks) {
    this.tracks = tracks;
  }

  public List<DeadlineDTO> getDeadlines() {
    return deadlines;
  }

  public void setDeadlines(List<DeadlineDTO> deadlines) {
    this.deadlines = deadlines;
  }

  public CFPResponseDTO getCfp() {
    return cfp;
  }

  public void setCfp(CFPResponseDTO cfp) {
    this.cfp = cfp;
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
    private String name;
    private String acronym;
    private String description;
    private Long chairId;
    private Boolean published;
    private String reviewMode;
    private List<TopicDTO> topics;
    private List<KeywordDTO> keywords;
    private List<TrackDTO> tracks;
    private List<DeadlineDTO> deadlines;
    private CFPResponseDTO cfp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder acronym(String acronym) {
      this.acronym = acronym;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder chairId(Long chairId) {
      this.chairId = chairId;
      return this;
    }

    public Builder published(Boolean published) {
      this.published = published;
      return this;
    }

    public Builder reviewMode(String reviewMode) {
      this.reviewMode = reviewMode;
      return this;
    }

    public Builder topics(List<TopicDTO> topics) {
      this.topics = topics;
      return this;
    }

    public Builder keywords(List<KeywordDTO> keywords) {
      this.keywords = keywords;
      return this;
    }

    public Builder tracks(List<TrackDTO> tracks) {
      this.tracks = tracks;
      return this;
    }

    public Builder deadlines(List<DeadlineDTO> deadlines) {
      this.deadlines = deadlines;
      return this;
    }

    public Builder cfp(CFPResponseDTO cfp) {
      this.cfp = cfp;
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

    public ConferenceResponseDTO build() {
      return new ConferenceResponseDTO(
          id, name, acronym, description, chairId, published, reviewMode, topics, keywords, tracks, deadlines, cfp,
          createdAt, updatedAt);
    }
  }
}
