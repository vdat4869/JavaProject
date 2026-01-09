package com.uth.confms.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  private String description;

  private String resource;

  private String action;

  public Permission() {}

  public Permission(
      Long id, String name, String description, String resource, String action) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.resource = resource;
    this.action = action;
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

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private String name;
    private String description;
    private String resource;
    private String action;

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

    public Builder resource(String resource) {
      this.resource = resource;
      return this;
    }

    public Builder action(String action) {
      this.action = action;
      return this;
    }

    public Permission build() {
      return new Permission(id, name, description, resource, action);
    }
  }
}
