package com.uth.confms.submission.dto;

import java.util.List;

public class SubmissionUpdateDTO {
  private String title;
  private String abstractText;
  private Long trackId;
  private String keywords;
  private List<SubmissionAuthorDTO> authors;

  public SubmissionUpdateDTO() {}

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAbstractText() {
    return abstractText;
  }

  public void setAbstractText(String abstractText) {
    this.abstractText = abstractText;
  }

  public Long getTrackId() {
    return trackId;
  }

  public void setTrackId(Long trackId) {
    this.trackId = trackId;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public List<SubmissionAuthorDTO> getAuthors() {
    return authors;
  }

  public void setAuthors(List<SubmissionAuthorDTO> authors) {
    this.authors = authors;
  }
}
