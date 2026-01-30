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
 * <p>
 * Một conference có thể có:
 *
 * <ul>
 * <li>Nhiều tracks (các track khác nhau)
 * <li>Nhiều deadlines (các mốc thời gian quan trọng)
 * <li>Một CFP (Call For Papers)
 * </ul>
 *
 * <p>
 * Conference có thể được publish (công khai) hoặc chưa publish.
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

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ReviewMode reviewMode = ReviewMode.DOUBLE_BLIND;

  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private AssignmentStrategy assignmentStrategy; // Chiến lược phân công (VD: Cân bằng tải, Dựa trên chuyên môn)

  @Column(columnDefinition = "TEXT")
  private String assignmentRules; // Các quy tắc phân công dạng JSON (ví dụ: {"requireExpertiseMatch": true})

  @Column(nullable = true)
  private Integer minReviewersPerSubmission = 3; // Số lượng reviewer tối thiểu cho mỗi bài

  @Column(nullable = true)
  private Integer maxReviewersPerSubmission = 5; // Số lượng reviewer tối đa cho mỗi bài

  @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Topic> topics = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "conference_keywords", joinColumns = @JoinColumn(name = "conference_id"), inverseJoinColumns = @JoinColumn(name = "keyword_id"))
  private List<Keyword> keywords = new ArrayList<>();

  @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Track> tracks = new ArrayList<>();

  @OneToMany(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Deadline> deadlines = new ArrayList<>();

  @OneToOne(mappedBy = "conference", cascade = CascadeType.ALL, orphanRemoval = true)
  private CFP cfp;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  public Conference() {
  }

  public Conference(
      Long id,
      String name,
      String acronym,
      String description,
      Long chairId,
      Boolean published,
      ReviewMode reviewMode,
      List<Topic> topics,
      List<Keyword> keywords,
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
    this.reviewMode = reviewMode != null ? reviewMode : ReviewMode.DOUBLE_BLIND;
    this.topics = topics != null ? topics : new ArrayList<>();
    this.keywords = keywords != null ? keywords : new ArrayList<>();
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

  public ReviewMode getReviewMode() {
    return reviewMode;
  }

  public void setReviewMode(ReviewMode reviewMode) {
    this.reviewMode = reviewMode != null ? reviewMode : ReviewMode.DOUBLE_BLIND;
  }

  public AssignmentStrategy getAssignmentStrategy() {
    return assignmentStrategy;
  }

  public void setAssignmentStrategy(AssignmentStrategy assignmentStrategy) {
    this.assignmentStrategy = assignmentStrategy;
  }

  public String getAssignmentRules() {
    return assignmentRules;
  }

  public void setAssignmentRules(String assignmentRules) {
    this.assignmentRules = assignmentRules;
  }

  public Integer getMinReviewersPerSubmission() {
    return minReviewersPerSubmission;
  }

  public void setMinReviewersPerSubmission(Integer minReviewersPerSubmission) {
    this.minReviewersPerSubmission = minReviewersPerSubmission;
  }

  public Integer getMaxReviewersPerSubmission() {
    return maxReviewersPerSubmission;
  }

  public void setMaxReviewersPerSubmission(Integer maxReviewersPerSubmission) {
    this.maxReviewersPerSubmission = maxReviewersPerSubmission;
  }

  public List<Topic> getTopics() {
    return topics;
  }

  public void setTopics(List<Topic> topics) {
    this.topics = topics != null ? topics : new ArrayList<>();
  }

  public List<Keyword> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<Keyword> keywords) {
    this.keywords = keywords != null ? keywords : new ArrayList<>();
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

  public enum ReviewMode {
    SINGLE_BLIND, // Reviewer knows author, author doesn't know reviewer
    DOUBLE_BLIND // Neither knows the other
  }

  /** Enum định nghĩa assignment strategy */
  /** Enum định nghĩa chiến lược phân công (Assignment Strategy) */
  public enum AssignmentStrategy {
    /** Cân bằng khối lượng công việc đều giữa các reviewer */
    BALANCED,
    /** Ưu tiên reviewer có chuyên môn phù hợp với bài nộp */
    EXPERTISE_BASED,
    /** Ưu tiên reviewer đang có ít việc */
    WORKLOAD_BASED,
    /** Kết hợp giữa chuyên môn và khối lượng công việc */
    HYBRID
  }

  public static class Builder {
    private Long id;
    private String name;
    private String acronym;
    private String description;
    private Long chairId;
    private Boolean published = false;
    private ReviewMode reviewMode = ReviewMode.DOUBLE_BLIND;
    private AssignmentStrategy assignmentStrategy;
    private String assignmentRules;
    private Integer minReviewersPerSubmission = 3;
    private Integer maxReviewersPerSubmission = 5;
    private List<Topic> topics = new ArrayList<>();
    private List<Keyword> keywords = new ArrayList<>();
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

    public Builder reviewMode(ReviewMode reviewMode) {
      this.reviewMode = reviewMode != null ? reviewMode : ReviewMode.DOUBLE_BLIND;
      return this;
    }

    public Builder assignmentStrategy(AssignmentStrategy assignmentStrategy) {
      this.assignmentStrategy = assignmentStrategy;
      return this;
    }

    public Builder assignmentRules(String assignmentRules) {
      this.assignmentRules = assignmentRules;
      return this;
    }

    public Builder minReviewersPerSubmission(Integer minReviewersPerSubmission) {
      this.minReviewersPerSubmission = minReviewersPerSubmission;
      return this;
    }

    public Builder maxReviewersPerSubmission(Integer maxReviewersPerSubmission) {
      this.maxReviewersPerSubmission = maxReviewersPerSubmission;
      return this;
    }

    public Builder topics(List<Topic> topics) {
      this.topics = topics != null ? topics : new ArrayList<>();
      return this;
    }

    public Builder keywords(List<Keyword> keywords) {
      this.keywords = keywords != null ? keywords : new ArrayList<>();
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
      Conference conference = new Conference(
          id, name, acronym, description, chairId, published, reviewMode, topics, keywords, tracks, deadlines, cfp,
          createdAt, updatedAt);
      // Set assignment preferences
      conference.setAssignmentStrategy(assignmentStrategy);
      conference.setAssignmentRules(assignmentRules);
      conference.setMinReviewersPerSubmission(minReviewersPerSubmission);
      conference.setMaxReviewersPerSubmission(maxReviewersPerSubmission);
      return conference;
    }
  }
}
