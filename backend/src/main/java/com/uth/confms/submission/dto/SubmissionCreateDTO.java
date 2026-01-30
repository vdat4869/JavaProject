package com.uth.confms.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO cho request tạo submission mới
 *
 * <p>
 * DTO này chứa thông tin cần thiết để tạo submission:
 *
 * <ul>
 * <li>conferenceId - ID của conference (required)
 * <li>title - Tiêu đề submission (required)
 * <li>abstractText - Tóm tắt submission (required)
 * <li>trackId - ID của track (optional)
 * <li>keywords - Từ khóa (optional)
 * <li>authors - Danh sách authors (optional)
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class SubmissionCreateDTO {
  @NotNull(message = "Conference ID is required")
  private Long conferenceId;

  @NotBlank(message = "Title is required")
  private String title; // Tiêu đề

  @NotBlank(message = "Abstract is required")
  private String abstractText; // Tóm tắt

  private Long trackId; // Track ID (nếu có)

  private String keywords; // Từ khóa

  private List<SubmissionAuthorDTO> authors; // Danh sách đồng tác giả

  public SubmissionCreateDTO() {
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

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
