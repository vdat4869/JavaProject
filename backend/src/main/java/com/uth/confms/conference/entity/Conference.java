package com.uth.confms.conference.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity đại diện cho hội nghị (Conference)
 *
 * <p>Một conference có thể có:
 *
 * <ul>
 *   <li>Nhiều tracks (các track khác nhau)
 *   <li>Nhiều deadlines (các mốc thời gian quan trọng)
 *   <li>Một CFP (Call For Papers)
 * </ul>
 *
 * <p>Conference có thể được publish (công khai) hoặc chưa publish.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "conferences")
@EntityListeners(AuditingEntityListener.class)
public class Conference {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String acronym;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private Long chairId;

  @Column(nullable = false)
  private Boolean published = false;

  @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Track> tracks = new ArrayList<>();

  @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Deadline> deadlines = new ArrayList<>();

  @OneToOne(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private CFP cfp;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  public Conference() {}

  public Conference(
      Long id,
      String name,
      String acronym,
      String description,
      Long chairId,
      Boolean published,
      List<Track> tracks,
      List<Deadline> deadlines,
      CFP cfp,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.acronym = acronym;
    this.description = description;
    this.chairId = chairId;
    this.published = published;
    this.tracks = tracks;
    this.deadlines = deadlines;
    this.cfp = cfp;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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

  public Long getChairId() {
    return chairId;
  }

  public void setChairId(Long chairId) {
    this.chairId = chairId;
  }

  public Boolean getPublished() {
    return published;
  }

  public void setPublished(Boolean published) {
    this.published = published;
  }

  public List<Track> getTracks() {
    return tracks;
  }

  public void setTracks(List<Track> tracks) {
    this.tracks = tracks;
  }

  public List<Deadline> getDeadlines() {
    return deadlines;
  }

  public void setDeadlines(List<Deadline> deadlines) {
    this.deadlines = deadlines;
  }

  public CFP getCfp() {
    return cfp;
  }

  public void setCfp(CFP cfp) {
    this.cfp = cfp;
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

  public static class Builder {
    private Long id;
    private String name;
    private String acronym;
    private String description;
    private Long chairId;
    private Boolean published = false;
    private List<Track> tracks = new ArrayList<>();
    private List<Deadline> deadlines = new ArrayList<>();
    private CFP cfp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder acronym(String acronym) {
      this.acronym = acronym;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder chairId(Long chairId) {
      this.chairId = chairId;
      return this;
    }

    public Builder published(Boolean published) {
      this.published = published;
      return this;
    }

    public Builder tracks(List<Track> tracks) {
      this.tracks = tracks;
      return this;
    }

    public Builder deadlines(List<Deadline> deadlines) {
      this.deadlines = deadlines;
      return this;
    }

    public Builder cfp(CFP cfp) {
      this.cfp = cfp;
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

    public Conference build() {
      return new Conference(
          id, name, acronym, description, chairId, published, tracks, deadlines, cfp, createdAt, updatedAt);
    }
  }
}
