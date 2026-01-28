package com.uth.confms.submission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO cho request cập nhật submission
 *
 * <p>
 * Fix 1.3: Thêm validation annotations để đảm bảo metadata không rỗng
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class SubmissionUpdateDTO {
  @Size(min = 1, max = 500, message = "Title must be between 1 and 500 characters")
  private String title;

  @Size(min = 1, max = 5000, message = "Abstract must be between 1 and 5000 characters")
  private String abstractText;

  private Long trackId;

  @Size(max = 1000, message = "Keywords must not exceed 1000 characters")
  private String keywords;

  @Valid
  private List<SubmissionAuthorDTO> authors;

  public SubmissionUpdateDTO() {
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
