package com.uth.confms.pc.entity;

import com.uth.confms.pc.entity.enums.ReviewStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Paper dai dien cho mot bai bao duoc submit
 * Luu tru thong tin ve tac gia, noi dung, va trang thai review
 */
public class Paper {
    private String id;
    private String title;
    private List<String> authors;
    private String authorInstitution;
    private List<String> keywords;
    private List<String> assignedReviewers;
    private ReviewStatus reviewStatus;

    public Paper() {
        this.authors = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.assignedReviewers = new ArrayList<>();
        this.reviewStatus = ReviewStatus.SUBMITTED;
    }

    public Paper(String id, String title, String authorInstitution) {
        this();
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

    public void addAuthor(String author) {
        if (!this.authors.contains(author)) {
            this.authors.add(author);
        }
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

    public void addKeyword(String keyword) {
        if (!this.keywords.contains(keyword)) {
            this.keywords.add(keyword);
        }
    }

    public List<String> getAssignedReviewers() {
        return assignedReviewers;
    }

    public void setAssignedReviewers(List<String> assignedReviewers) {
        this.assignedReviewers = assignedReviewers;
    }

    public void assignReviewer(String reviewerId) {
        if (!this.assignedReviewers.contains(reviewerId)) {
            this.assignedReviewers.add(reviewerId);
        }
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    @Override
    public String toString() {
        return "Paper{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", authors=" + authors +
                ", authorInstitution='" + authorInstitution + '\'' +
                ", reviewStatus=" + reviewStatus +
                ", assignedReviewers=" + assignedReviewers.size() +
                '}';
    }
}

