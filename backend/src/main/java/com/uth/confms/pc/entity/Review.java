package com.uth.confms.pc.entity;

import com.uth.confms.pc.entity.enums.ReviewProgressStatus;

/**
 * Entity Review dai dien cho mot review cua PC member cho mot paper
 * Luu tru thong tin ve tien do va ket qua review
 */
public class Review {
    private String id;
    private String pcMemberId;
    private String paperId;
    private ReviewProgressStatus status;
    private Double score;
    private String comment;

    public Review() {
        this.status = ReviewProgressStatus.ASSIGNED;
    }

    public Review(String id, String pcMemberId, String paperId) {
        this();
        this.id = id;
        this.pcMemberId = pcMemberId;
        this.paperId = paperId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPcMemberId() {
        return pcMemberId;
    }

    public void setPcMemberId(String pcMemberId) {
        this.pcMemberId = pcMemberId;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public ReviewProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewProgressStatus status) {
        this.status = status;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCompleted() {
        return status == ReviewProgressStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", pcMemberId='" + pcMemberId + '\'' +
                ", paperId='" + paperId + '\'' +
                ", status=" + status +
                ", score=" + score +
                '}';
    }
}

