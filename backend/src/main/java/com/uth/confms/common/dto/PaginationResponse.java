package com.uth.confms.common.dto;

import java.util.List;

public class PaginationResponse<T> {
  private List<T> content; // Nội dung trang hiện tại
  private Integer page; // Số trang
  private Integer size; // Kích thước trang
  private Long totalElements; // Tổng số phần tử
  private Integer totalPages; // Tổng số trang
  private Boolean first;
  private Boolean last;

  public PaginationResponse() {
  }

  public PaginationResponse(
      List<T> content,
      Integer page,
      Integer size,
      Long totalElements,
      Integer totalPages,
      Boolean first,
      Boolean last) {
    this.content = content;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.first = first;
    this.last = last;
  }

  public static <T> PaginationResponse<T> of(
      List<T> content, PaginationRequest request, Long totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

    return PaginationResponse.<T>builder()
        .content(content)
        .page(request.getPage())
        .size(request.getSize())
        .totalElements(totalElements)
        .totalPages(totalPages)
        .first(request.getPage() == 0)
        .last(request.getPage() >= totalPages - 1)
        .build();
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public List<T> getContent() {
    return content;
  }

  public void setContent(List<T> content) {
    this.content = content;
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

  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
  }

  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public Boolean getFirst() {
    return first;
  }

  public void setFirst(Boolean first) {
    this.first = first;
  }

  public Boolean getLast() {
    return last;
  }

  public void setLast(Boolean last) {
    this.last = last;
  }

  public static class Builder<T> {
    private List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;

    public Builder<T> content(List<T> content) {
      this.content = content;
      return this;
    }

    public Builder<T> page(Integer page) {
      this.page = page;
      return this;
    }

    public Builder<T> size(Integer size) {
      this.size = size;
      return this;
    }

    public Builder<T> totalElements(Long totalElements) {
      this.totalElements = totalElements;
      return this;
    }

    public Builder<T> totalPages(Integer totalPages) {
      this.totalPages = totalPages;
      return this;
    }

    public Builder<T> first(Boolean first) {
      this.first = first;
      return this;
    }

    public Builder<T> last(Boolean last) {
      this.last = last;
      return this;
    }

    public PaginationResponse<T> build() {
      return new PaginationResponse<>(
          content, page, size, totalElements, totalPages, first, last);
    }
  }
}
