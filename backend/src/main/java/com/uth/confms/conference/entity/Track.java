package com.uth.confms.conference.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tracks")
public class Track {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "conference_id", nullable = false)
  private Conference conference;

  @Column(nullable = false)
  private String name; // Tên track (Lĩnh vực nghiên cứu)

  @Column(columnDefinition = "TEXT")
  private String description; // Mô tả track

  @Column(nullable = false)
  private Boolean active = true; // Trạng thái hoạt động

  public Track() {
  }

  public Track(Long id, Conference conference, String name, String description, Boolean active) {
    this.id = id;
    this.conference = conference;
    this.name = name;
    this.description = description;
    this.active = active;
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

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public static class Builder {
    private Long id;
    private Conference conference;
    private String name;
    private String description;
    private Boolean active = true;

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

    public Builder active(Boolean active) {
      this.active = active;
      return this;
    }

    public Track build() {
      return new Track(id, conference, name, description, active);
    }
  }
}
