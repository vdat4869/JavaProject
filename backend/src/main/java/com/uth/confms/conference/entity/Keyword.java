package com.uth.confms.conference.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho keyword (từ khóa) của conference
 *
 * <p>
 * Keywords được sử dụng để tag và categorize conferences.
 * Một keyword có thể được sử dụng bởi nhiều conferences (ManyToMany).
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Entity
@Table(name = "keywords")
public class Keyword {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name; // Tên từ khóa

  private String description; // Mô tả từ khóa

  @ManyToMany(mappedBy = "keywords", fetch = FetchType.LAZY)
  private List<Conference> conferences = new ArrayList<>();

  public Keyword() {
  }

  public Keyword(Long id, String name, String description, List<Conference> conferences) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.conferences = conferences != null ? conferences : new ArrayList<>();
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Conference> getConferences() {
    return conferences;
  }

  public void setConferences(List<Conference> conferences) {
    this.conferences = conferences != null ? conferences : new ArrayList<>();
  }

  public static class Builder {
    private Long id;
    private String name;
    private String description;
    private List<Conference> conferences = new ArrayList<>();

    public Builder id(Long id) {
      this.id = id;
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

    public Builder conferences(List<Conference> conferences) {
      this.conferences = conferences != null ? conferences : new ArrayList<>();
      return this;
    }

    public Keyword build() {
      return new Keyword(id, name, description, conferences);
    }
  }
}
