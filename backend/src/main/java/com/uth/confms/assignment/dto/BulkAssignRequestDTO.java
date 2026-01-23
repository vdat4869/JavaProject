package com.uth.confms.assignment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO cho request bulk assign reviewers
 *
 * <p>DTO này được sử dụng khi chair muốn assign nhiều reviewers cho nhiều submissions cùng lúc.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class BulkAssignRequestDTO {
  @NotEmpty(message = "Assignments list cannot be empty")
  @Valid
  private List<AssignmentCreateDTO> assignments;

  public BulkAssignRequestDTO() {}

  public List<AssignmentCreateDTO> getAssignments() {
    return assignments;
  }

  public void setAssignments(List<AssignmentCreateDTO> assignments) {
    this.assignments = assignments;
  }
}
