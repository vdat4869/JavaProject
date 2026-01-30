package com.uth.confms.conference.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "cfps")
@EntityListeners(AuditingEntityListener.class)
public class CFP {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "conference_id", nullable = false)
  private Conference conference;

  @Column(columnDefinition = "TEXT")
  private String callForPapers; // Nội dung lời mời viết bài

  @Column(columnDefinition = "TEXT")
  private String topics; // Danh sách chủ đề (để tương thích ngược)

  @Column(columnDefinition = "TEXT")
  private String submissionGuidelines; // Hướng dẫn nộp bài

  @Column(nullable = false)
  private Boolean open = false; // Trạng thái mở/đóng nộp bài

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public CFP() {
  }

  public CFP(
      Long id,
      Conference conference,
      String callForPapers,
      String topics,
      String submissionGuidelines,
      Boolean open,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.conference = conference;
    this.callForPapers = callForPapers;
    this.topics = topics;
    this.submissionGuidelines = submissionGuidelines;
    this.open = open;
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

  public Conference getConference() {
    return conference;
  }

  public void setConference(Conference conference) {
    this.conference = conference;
  }

  public String getCallForPapers() {
    return callForPapers;
  }

  public void setCallForPapers(String callForPapers) {
    this.callForPapers = callForPapers;
  }

  public String getTopics() {
    return topics;
  }

  public void setTopics(String topics) {
    this.topics = topics;
  }

  public String getSubmissionGuidelines() {
    return submissionGuidelines;
  }

  public void setSubmissionGuidelines(String submissionGuidelines) {
    this.submissionGuidelines = submissionGuidelines;
  }

  public Boolean getOpen() {
    return open;
  }

  public void setOpen(Boolean open) {
    this.open = open;
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
    private Conference conference;
    private String callForPapers;
    private String topics;
    private String submissionGuidelines;
    private Boolean open = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder conference(Conference conference) {
      this.conference = conference;
      return this;
    }

    public Builder callForPapers(String callForPapers) {
      this.callForPapers = callForPapers;
      return this;
    }

    public Builder topics(String topics) {
      this.topics = topics;
      return this;
    }

    public Builder submissionGuidelines(String submissionGuidelines) {
      this.submissionGuidelines = submissionGuidelines;
      return this;
    }

    public Builder open(Boolean open) {
      this.open = open;
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

    public CFP build() {
      return new CFP(id, conference, callForPapers, topics, submissionGuidelines, open, createdAt, updatedAt);
    }
  }
}
