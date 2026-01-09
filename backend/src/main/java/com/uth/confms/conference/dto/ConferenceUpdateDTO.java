package com.uth.confms.conference.dto;

import java.util.List;

public class ConferenceUpdateDTO {
  private String name;
  private String acronym;
  private String description;
  private Boolean published;
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
