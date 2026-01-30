package com.uth.confms.conference.dto;

public class TrackDTO {
  private Long id;
  private String name; // Tên track
  private String description; // Mô tả
  private Boolean active; // Trạng thái hoạt động

  public TrackDTO() {
  }

  public TrackDTO(Long id, String name, String description, Boolean active) {
    this.id = id;
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
    private String name;
    private String description;
    private Boolean active;

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

    public Builder active(Boolean active) {
      this.active = active;
      return this;
    }

    public TrackDTO build() {
      return new TrackDTO(id, name, description, active);
    }
  }
}
