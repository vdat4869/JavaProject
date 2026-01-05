package com.uth.confms.pc.controller;

import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.service.PCManagementService;
import java.util.List;

/**
 * Controller chuyen biet cho Reviewer Suggestion (AI feature)
 */
public class ReviewerSuggestionController {
    
    private PCManagementService service;
    private static final int DEFAULT_SUGGESTION_COUNT = 5;

    public ReviewerSuggestionController(PCManagementService service) {
        this.service = service;
    }
    
    public List<PCMember> suggestReviewers(String paperId, int topN) {
        if (paperId == null || paperId.trim().isEmpty()) {
            throw new IllegalArgumentException("Paper ID khong duoc rong");
        }
        if (topN <= 0) {
            topN = DEFAULT_SUGGESTION_COUNT;
        }
        return service.suggestReviewers(paperId, topN);
    }
    
    public List<PCMember> suggestReviewers(String paperId) {
        return suggestReviewers(paperId, DEFAULT_SUGGESTION_COUNT);
    }
}

