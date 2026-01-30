package com.uth.confms.conference.dto;

import java.time.LocalDateTime;

public class DeadlineDTO {
  private Long id;
  private String type; // Loại hạn chót
  private LocalDateTime dueDate; // Ngày hết hạn
  private String description; // Mô tả
  private Boolean hardDeadline; // Hạn chót cứng

  public DeadlineDTO() {
  }

  public DeadlineDTO(
      Long id, String type, LocalDateTime dueDate, String description, Boolean hardDeadline) {
    this.id = id;
    this.type = type;
    this.dueDate = dueDate;
    this.description = description;
    this.hardDeadline = hardDeadline;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = dueDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getHardDeadline() {
    return hardDeadline;
  }

  public void setHardDeadline(Boolean hardDeadline) {
    this.hardDeadline = hardDeadline;
  }

  public static class Builder {
    private Long id;
    private String type;
    private LocalDateTime dueDate;
    private String description;
    private Boolean hardDeadline;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder dueDate(LocalDateTime dueDate) {
      this.dueDate = dueDate;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder hardDeadline(Boolean hardDeadline) {
      this.hardDeadline = hardDeadline;
      return this;
    }

    public DeadlineDTO build() {
      return new DeadlineDTO(id, type, dueDate, description, hardDeadline);
    }
  }
}
