package com.uth.confms.conference.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * DTO cho request tạo conference mới
 *
 * <p>DTO này chứa thông tin cần thiết để tạo conference:
 *
 * <ul>
 *   <li>name - Tên conference (required)
 *   <li>acronym - Tên viết tắt (optional)
 *   <li>description - Mô tả (optional)
 *   <li>tracks - Danh sách tracks (optional)
 *   <li>deadlines - Danh sách deadlines (optional)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class ConferenceCreateDTO {
  @NotBlank private String name;

  private String acronym;

  private String description;

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
