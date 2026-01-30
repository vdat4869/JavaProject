package com.uth.confms.conference.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "topics")
public class Topic {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "conference_id", nullable = false)
  private Conference conference;

  @Column(nullable = false)
  private String name; // Tên chủ đề

  @Column(columnDefinition = "TEXT")
  private String description; // Mô tả chủ đề

  public Topic() {
  }

  public Topic(Long id, Conference conference, String name, String description) {
    this.id = id;
    this.conference = conference;
    this.name = name;
    this.description = description;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public static class Builder {
    private Long id;
    private Conference conference;
    private String name;
    private String description;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conference(Conference conference) {
      this.conference = conference;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Topic build() {
      return new Topic(id, conference, name, description);
    }
  }
}
