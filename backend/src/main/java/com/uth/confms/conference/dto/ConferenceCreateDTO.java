package com.uth.confms.conference.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO cho request tạo conference mới
 *
 * <p>
 * DTO này chứa thông tin cần thiết để tạo conference:
 *
 * <ul>
 * <li>name - Tên conference (required)
 * <li>acronym - Tên viết tắt (optional)
 * <li>description - Mô tả (optional)
 * <li>tracks - Danh sách tracks (optional)
 * <li>deadlines - Danh sách deadlines (optional)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class ConferenceCreateDTO {
  @NotBlank
  private String name; // Tên hội nghị

  private String acronym; // Tên viết tắt

  private String description; // Mô tả hội nghị

  private String reviewMode; // Chế độ review (SINGLE_BLIND, DOUBLE_BLIND)

  private List<TopicDTO> topics; // Danh sách các chủ đề

  private List<Long> keywordIds; // Danh sách ID các từ khóa

  private List<TrackDTO> tracks; // Danh sách các track (lĩnh vực)

  private List<DeadlineDTO> deadlines; // Danh sách các mốc thời gian

  private Long chairId; // ID của Chair (Chỉ dành cho Admin khi tạo hộ)

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

  public Long getChairId() {
    return chairId;
  }

  public void setChairId(Long chairId) {
    this.chairId = chairId;
  }
}
