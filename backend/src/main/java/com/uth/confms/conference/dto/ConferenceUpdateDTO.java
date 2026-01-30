package com.uth.confms.conference.dto;

import java.util.List;

public class ConferenceUpdateDTO {
  private String name; // Tên hội nghị
  private String acronym; // Tên viết tắt
  private String description; // Mô tả
  private Boolean published; // Trạng thái công khai
  private String reviewMode; // Chế độ review (SINGLE_BLIND, DOUBLE_BLIND)
  private List<TopicDTO> topics; // Danh sách chủ đề
  private List<Long> keywordIds; // Danh sách ID từ khóa
  private List<TrackDTO> tracks; // Danh sách tracks
  private List<DeadlineDTO> deadlines; // Danh sách deadlines

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
