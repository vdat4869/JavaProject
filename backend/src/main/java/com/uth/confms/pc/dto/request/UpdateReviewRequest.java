package com.uth.confms.pc.dto.request;

/**
 * DTO request cho viec cap nhat review
 */
public class UpdateReviewRequest {
    private String reviewId;
    private String status;
    private Double score;
    private String comment;

    public UpdateReviewRequest() {
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
}



