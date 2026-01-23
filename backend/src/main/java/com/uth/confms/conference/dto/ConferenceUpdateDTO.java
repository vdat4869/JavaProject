package com.uth.confms.conference.dto;

import java.util.List;

public class ConferenceUpdateDTO {
  private String name;
  private String acronym;
  private String description;
  private Boolean published;
  private String reviewMode; // SINGLE_BLIND or DOUBLE_BLIND
  private List<TopicDTO> topics;
  private List<Long> keywordIds; // Reference to existing keywords
  private List<TrackDTO> tracks;
  private List<DeadlineDTO> deadlines;

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

  public List<Long> getKeywordIds() {
    return keywordIds;
  }

  public void setKeywordIds(List<Long> keywordIds) {
    this.keywordIds = keywordIds;
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
}
