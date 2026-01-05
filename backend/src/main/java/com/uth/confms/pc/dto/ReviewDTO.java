package com.uth.confms.pc.dto;

/**
 * DTO cho Review - dung de truyen du lieu giua cac layer
 */
public class ReviewDTO {
    private String id;
    private String pcMemberId;
    private String paperId;
    private String status;
    private Double score;
    private String comment;

    public ReviewDTO() {
    }

    public ReviewDTO(String id, String pcMemberId, String paperId) {
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

