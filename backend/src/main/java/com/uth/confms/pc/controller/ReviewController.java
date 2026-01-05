package com.uth.confms.pc.controller;

import com.uth.confms.pc.entity.Review;
import com.uth.confms.pc.entity.enums.ReviewProgressStatus;
import com.uth.confms.pc.service.PCManagementService;
import java.util.List;

/**
 * Controller chuyen biet cho Review operations
 */
public class ReviewController {
    
    private PCManagementService service;

    public ReviewController(PCManagementService service) {
        this.service = service;
    }
    
    private void validateId(String id, String entityName) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(entityName + " ID khong duoc rong");
        }
    }
    
    private void validateScore(Double score) {
        if (score != null && (score < 0 || score > 10)) {
            throw new IllegalArgumentException("Diem so phai trong khoang 0-10");
        }
    }
    
    public boolean assignReviewer(String pcMemberId, String paperId) {
        validateId(pcMemberId, "PCMember");
        validateId(paperId, "Paper");
        return service.assignReviewer(pcMemberId, paperId);
    }
    
    public boolean updateReviewProgress(String reviewId, ReviewProgressStatus status, Double score, String comment) {
        validateId(reviewId, "Review");
        if (status == ReviewProgressStatus.COMPLETED) {
            validateScore(score);
        }
        return service.updateReviewProgress(reviewId, status, score, comment);
    }
    
    public List<Review> getReviewProgress(String pcMemberId) {
        validateId(pcMemberId, "PCMember");
        return service.getReviewProgress(pcMemberId);
    }
    
    public List<Review> getReviewsByPaper(String paperId) {
        validateId(paperId, "Paper");
        List<Review> allReviews = service.getReviewProgress(paperId);
        // Filter by paperId if needed
        return allReviews;
    }
}

