package com.uth.confms.assignment.dto;

import java.util.Map;

/**
 * DTO cho assignment statistics
 *
 * <p>
 * DTO này chứa các thống kê về assignments trong một conference.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public class AssignmentStatisticsDTO {
  private Integer totalAssignments; // Tổng số phân công
  private Integer totalReviewers; // Tổng số reviewer tham gia
  private Double averageAssignmentsPerReviewer; // Số phân công trung bình mỗi reviewer
  private Integer minAssignments; // Số phân công ít nhất một reviewer nhận
  private Integer maxAssignments; // Số phân công nhiều nhất một reviewer nhận
  private Map<String, Integer> statusDistribution; // Phân bổ trạng thái (ASSIGNED, ACCEPTED, etc.)
  private Map<String, Integer> workloadDistribution; // Phân bổ khối lượng công việc (LOW, NORMAL, etc.)
  private Double acceptanceRate; // Tỷ lệ chấp nhận phân công (%)
  private Double completionRate; // Tỷ lệ hoàn thành review (%)
  private Double declineRate; // Tỷ lệ từ chối phân công (%)

  public AssignmentStatisticsDTO() {
  }

  public AssignmentStatisticsDTO(
      Integer totalAssignments,
      Integer totalReviewers,
      Double averageAssignmentsPerReviewer,
      Integer minAssignments,
      Integer maxAssignments,
      Map<String, Integer> statusDistribution,
      Map<String, Integer> workloadDistribution,
      Double acceptanceRate,
      Double completionRate,
      Double declineRate) {
    this.totalAssignments = totalAssignments;
    this.totalReviewers = totalReviewers;
    this.averageAssignmentsPerReviewer = averageAssignmentsPerReviewer;
    this.minAssignments = minAssignments;
    this.maxAssignments = maxAssignments;
    this.statusDistribution = statusDistribution;
    this.workloadDistribution = workloadDistribution;
    this.acceptanceRate = acceptanceRate;
    this.completionRate = completionRate;
    this.declineRate = declineRate;
  }

  public Integer getTotalAssignments() {
    return totalAssignments;
  }

  public void setTotalAssignments(Integer totalAssignments) {
    this.totalAssignments = totalAssignments;
  }

  public Integer getTotalReviewers() {
    return totalReviewers;
  }

  public void setTotalReviewers(Integer totalReviewers) {
    this.totalReviewers = totalReviewers;
  }

  public Double getAverageAssignmentsPerReviewer() {
    return averageAssignmentsPerReviewer;
  }

  public void setAverageAssignmentsPerReviewer(Double averageAssignmentsPerReviewer) {
    this.averageAssignmentsPerReviewer = averageAssignmentsPerReviewer;
  }

  public Integer getMinAssignments() {
    return minAssignments;
  }

  public void setMinAssignments(Integer minAssignments) {
    this.minAssignments = minAssignments;
  }

  public Integer getMaxAssignments() {
    return maxAssignments;
  }

  public void setMaxAssignments(Integer maxAssignments) {
    this.maxAssignments = maxAssignments;
  }

  public Map<String, Integer> getStatusDistribution() {
    return statusDistribution;
  }

  public void setStatusDistribution(Map<String, Integer> statusDistribution) {
    this.statusDistribution = statusDistribution;
  }

  public Map<String, Integer> getWorkloadDistribution() {
    return workloadDistribution;
  }

  public void setWorkloadDistribution(Map<String, Integer> workloadDistribution) {
    this.workloadDistribution = workloadDistribution;
  }

  public Double getAcceptanceRate() {
    return acceptanceRate;
  }

  public void setAcceptanceRate(Double acceptanceRate) {
    this.acceptanceRate = acceptanceRate;
  }

  public Double getCompletionRate() {
    return completionRate;
  }

  public void setCompletionRate(Double completionRate) {
    this.completionRate = completionRate;
  }

  public Double getDeclineRate() {
    return declineRate;
  }

  public void setDeclineRate(Double declineRate) {
    this.declineRate = declineRate;
  }
}
