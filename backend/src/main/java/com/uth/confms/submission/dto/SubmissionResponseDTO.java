package com.uth.confms.submission.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SubmissionResponseDTO {
  private Long id;
  private Long conferenceId;
  private Long authorId;
  private String title;
  private String abstractText;
  private String status;
  private String pdfFilePath;
  private Long trackId;
  private String keywords;
  private Boolean withdrawn;
  private Boolean canEdit; // Có thể chỉnh sửa?
  private Boolean canWithdraw; // Có thể rút?
  private List<SubmissionAuthorDTO> authors; // Danh sách tác giả
  private List<SubmissionFileDTO> files; // Lịch sử file
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String reviewMode; // Chế độ review (Single/Double Blind)
  private String trackName;

  public SubmissionResponseDTO() {
  }

  public SubmissionResponseDTO(
      Long id,
      Long conferenceId,
      Long authorId,
      String title,
      String abstractText,
      String status,
      String pdfFilePath,
      Long trackId,
      String keywords,
      Boolean withdrawn,
      List<SubmissionAuthorDTO> authors,
      List<SubmissionFileDTO> files,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      Boolean canEdit,
      Boolean canWithdraw,
      String reviewMode,
      String trackName) {
    this.id = id;
    this.conferenceId = conferenceId;
    this.authorId = authorId;
    this.title = title;
    this.abstractText = abstractText;
    this.status = status;
    this.pdfFilePath = pdfFilePath;
    this.trackId = trackId;
    this.keywords = keywords;
    this.withdrawn = withdrawn;
    this.authors = authors;
    this.files = files;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.canEdit = canEdit;
    this.canWithdraw = canWithdraw;
    this.reviewMode = reviewMode;
    this.trackName = trackName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getConferenceId() {
    return conferenceId;
  }

  public void setConferenceId(Long conferenceId) {
    this.conferenceId = conferenceId;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPdfFilePath() {
    return pdfFilePath;
  }

  public void setPdfFilePath(String pdfFilePath) {
    this.pdfFilePath = pdfFilePath;
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

  public Boolean getWithdrawn() {
    return withdrawn;
  }

  public void setWithdrawn(Boolean withdrawn) {
    this.withdrawn = withdrawn;
  }

  public List<SubmissionAuthorDTO> getAuthors() {
    return authors;
  }

  public void setAuthors(List<SubmissionAuthorDTO> authors) {
    this.authors = authors;
  }

  public List<SubmissionFileDTO> getFiles() {
    return files;
  }

  public void setFiles(List<SubmissionFileDTO> files) {
    this.files = files;
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

  public Boolean getCanEdit() {
    return canEdit;
  }

  public void setCanEdit(Boolean canEdit) {
    this.canEdit = canEdit;
  }

  public Boolean getCanWithdraw() {
    return canWithdraw;
  }

  public void setCanWithdraw(Boolean canWithdraw) {
    this.canWithdraw = canWithdraw;
  }

  public String getReviewMode() {
    return reviewMode;
  }

  public void setReviewMode(String reviewMode) {
    this.reviewMode = reviewMode;
  }

  public String getTrackName() {
    return trackName;
  }

  public void setTrackName(String trackName) {
    this.trackName = trackName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private Long conferenceId;
    private Long authorId;
    private String title;
    private String abstractText;
    private String status;
    private String pdfFilePath;
    private Long trackId;
    private String keywords;
    private Boolean withdrawn;
    private List<SubmissionAuthorDTO> authors;
    private List<SubmissionFileDTO> files;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reviewMode;
    private Boolean canEdit;
    private Boolean canWithdraw;
    private String trackName;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder trackName(String trackName) {
      this.trackName = trackName;
      return this;
    }

    // ... existing builder methods ...

    public Builder canEdit(Boolean canEdit) {
      this.canEdit = canEdit;
      return this;
    }

    public Builder canWithdraw(Boolean canWithdraw) {
      this.canWithdraw = canWithdraw;
      return this;
    }

    public SubmissionResponseDTO build() {
      return new SubmissionResponseDTO(
          id,
          conferenceId,
          authorId,
          title,
          abstractText,
          status,
          pdfFilePath,
          trackId,
          keywords,
          withdrawn,
          authors,
          files,
          createdAt,
          updatedAt,
          canEdit,
          canWithdraw,
          reviewMode,
          trackName);
    }

    public Builder conferenceId(Long conferenceId) {
      this.conferenceId = conferenceId;
      return this;
    }

    public Builder authorId(Long authorId) {
      this.authorId = authorId;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder abstractText(String abstractText) {
      this.abstractText = abstractText;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder pdfFilePath(String pdfFilePath) {
      this.pdfFilePath = pdfFilePath;
      return this;
    }

    public Builder trackId(Long trackId) {
      this.trackId = trackId;
      return this;
    }

    public Builder keywords(String keywords) {
      this.keywords = keywords;
      return this;
    }

    public Builder withdrawn(Boolean withdrawn) {
      this.withdrawn = withdrawn;
      return this;
    }

    public Builder authors(List<SubmissionAuthorDTO> authors) {
      this.authors = authors;
      return this;
    }

    public Builder files(List<SubmissionFileDTO> files) {
      this.files = files;
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

    public Builder reviewMode(String reviewMode) {
      this.reviewMode = reviewMode;
      return this;
    }

  }
}
