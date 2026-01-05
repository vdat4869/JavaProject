package com.uth.confms.pc.dto;

import java.util.List;

/**
 * DTO cho Paper - dung de truyen du lieu giua cac layer
 */
public class PaperDTO {
    private String id;
    private String title;
    private List<String> authors;
    private String authorInstitution;
    private List<String> keywords;
    private String reviewStatus;
    private int assignedReviewersCount;

    public PaperDTO() {
    }

    public PaperDTO(String id, String title, String authorInstitution) {
        this.id = id;
        this.title = title;
        this.authorInstitution = authorInstitution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getAuthorInstitution() {
        return authorInstitution;
    }

    public void setAuthorInstitution(String authorInstitution) {
        this.authorInstitution = authorInstitution;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public int getAssignedReviewersCount() {
        return assignedReviewersCount;
    }

    public void setAssignedReviewersCount(int assignedReviewersCount) {
        this.assignedReviewersCount = assignedReviewersCount;
    }
}

