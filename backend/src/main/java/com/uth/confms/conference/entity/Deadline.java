package com.uth.confms.conference.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deadlines")
public class Deadline {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "conference_id", nullable = false)
  private Conference conference;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DeadlineType type; // Loại hạn chót

  @Column(nullable = false)
  private LocalDateTime dueDate; // Ngày giờ hạn chót

  @Column(columnDefinition = "TEXT")
  private String description; // Mô tả thêm

  @Column(nullable = false)
  private Boolean hardDeadline = true; // Hạn chót cứng (không thể nộp trễ)

  public Deadline() {
  }

  public Deadline(
      Long id,
      Conference conference,
      DeadlineType type,
      LocalDateTime dueDate,
      String description,
      Boolean hardDeadline) {
    this.id = id;
    this.conference = conference;
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

  public Conference getConference() {
    return conference;
  }

  public void setConference(Conference conference) {
    this.conference = conference;
  }

  public DeadlineType getType() {
    return type;
  }

  public void setType(DeadlineType type) {
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

  public enum DeadlineType {
    SUBMISSION,
    REVIEW,
    REBUTTAL,
    DECISION,
    CAMERA_READY,
    PUBLICATION
  }

  public static class Builder {
    private Long id;
    private Conference conference;
    private DeadlineType type;
    private LocalDateTime dueDate;
    private String description;
    private Boolean hardDeadline = true;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conference(Conference conference) {
      this.conference = conference;
      return this;
    }

    public Builder type(DeadlineType type) {
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

    public Deadline build() {
      return new Deadline(id, conference, type, dueDate, description, hardDeadline);
    }
  }
}
