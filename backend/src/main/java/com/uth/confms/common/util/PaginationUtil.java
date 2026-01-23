package com.uth.confms.common.util;

import com.uth.confms.common.dto.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class để convert giữa PaginationRequest và Spring Data Pageable
 *
 * <p>Provides methods để:
 * <ul>
 *   <li>Convert PaginationRequest → Pageable
 *   <li>Convert Pageable → PaginationRequest
 *   <li>Create Sort từ PaginationRequest
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class PaginationUtil {

  /**
   * Convert PaginationRequest to Spring Data Pageable
   *
   * @param request PaginationRequest
   * @return Pageable object
   */
  public static Pageable toPageable(PaginationRequest request) {
    if (request == null) {
      return PageRequest.of(0, 20); // Default
    }

    Sort sort = createSort(request.getSortBy(), request.getSortDirection());
    
    if (sort != null) {
      return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
    
    return PageRequest.of(request.getPage(), request.getSize());
  }

  /**
   * Create Sort object từ sortBy và sortDirection
   *
   * @param sortBy Field name để sort
   * @param sortDirection "ASC" hoặc "DESC"
   * @return Sort object hoặc null nếu không có sortBy
   */
  public static Sort createSort(String sortBy, String sortDirection) {
    if (sortBy == null || sortBy.trim().isEmpty()) {
      return null;
    }

    Sort.Direction direction = Sort.Direction.ASC;
    if (sortDirection != null && sortDirection.equalsIgnoreCase("DESC")) {
      direction = Sort.Direction.DESC;
    }

    return Sort.by(direction, sortBy);
  }

  /**
   * Convert Spring Data Pageable to PaginationRequest
   *
   * @param pageable Pageable object
   * @return PaginationRequest
   */
  public static PaginationRequest fromPageable(Pageable pageable) {
    if (pageable == null) {
      return PaginationRequest.builder()
          .page(0)
          .size(20)
          .build();
    }

    String sortBy = null;
    String sortDirection = null;

    if (pageable.getSort() != null && pageable.getSort().isSorted()) {
      Sort.Order order = pageable.getSort().iterator().next();
      sortBy = order.getProperty();
      sortDirection = order.getDirection().name();
    }

    return PaginationRequest.builder()
        .page(pageable.getPageNumber())
        .size(pageable.getPageSize())
        .sortBy(sortBy)
        .sortDirection(sortDirection)
        .build();
  }

  /**
   * Create default PaginationRequest
   *
   * @return PaginationRequest với default values (page=0, size=20)
   */
  public static PaginationRequest defaultRequest() {
    return PaginationRequest.builder()
        .page(0)
        .size(20)
        .build();
  }

  /**
   * Create PaginationRequest với custom page và size
   *
   * @param page Page number (0-based)
   * @param size Page size
   * @return PaginationRequest
   */
  public static PaginationRequest of(int page, int size) {
    return PaginationRequest.builder()
        .page(page)
        .size(size)
        .build();
  }

  /**
   * Create PaginationRequest với sorting
   *
   * @param page Page number (0-based)
   * @param size Page size
   * @param sortBy Field name để sort
   * @param sortDirection "ASC" hoặc "DESC"
   * @return PaginationRequest
   */
  public static PaginationRequest of(int page, int size, String sortBy, String sortDirection) {
    return PaginationRequest.builder()
        .page(page)
        .size(size)
        .sortBy(sortBy)
        .sortDirection(sortDirection)
        .build();
  }
}
