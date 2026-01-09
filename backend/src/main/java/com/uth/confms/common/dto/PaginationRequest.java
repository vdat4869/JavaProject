package com.uth.confms.common.dto;

import jakarta.validation.constraints.Min;

public class PaginationRequest {
  @Min(0)
  private Integer page = 0;

  @Min(1)
  private Integer size = 20;

  private String sortBy;

  private String sortDirection; // ASC, DESC

  public PaginationRequest() {}

  public PaginationRequest(Integer page, Integer size, String sortBy, String sortDirection) {
    this.page = page;
    this.size = size;
    this.sortBy = sortBy;
    this.sortDirection = sortDirection;
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getOffset() {
    return page * size;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }

  public String getSortDirection() {
    return sortDirection;
  }

  public void setSortDirection(String sortDirection) {
    this.sortDirection = sortDirection;
  }

  public static class Builder {
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy;
    private String sortDirection;

    public Builder page(Integer page) {
      this.page = page;
      return this;
    }

    public Builder size(Integer size) {
      this.size = size;
      return this;
    }

    public Builder sortBy(String sortBy) {
      this.sortBy = sortBy;
      return this;
    }

    public Builder sortDirection(String sortDirection) {
      this.sortDirection = sortDirection;
      return this;
    }

    public PaginationRequest build() {
      return new PaginationRequest(page, size, sortBy, sortDirection);
    }
  }
}
