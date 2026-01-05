package com.uth.confms.pc.controller;

import com.uth.confms.pc.entity.Paper;
import com.uth.confms.pc.service.PCManagementService;
import java.util.List;

/**
 * Controller chuyen biet cho Paper operations
 */
public class PaperController {
    
    private PCManagementService service;

    public PaperController(PCManagementService service) {
        this.service = service;
    }
    
    private void validateId(String id, String entityName) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(entityName + " ID khong duoc rong");
        }
    }
    
    private void validatePaper(Paper paper) {
        if (paper == null) {
            throw new IllegalArgumentException("Paper khong duoc null");
        }
        if (paper.getId() == null || paper.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Paper ID khong duoc rong");
        }
        if (paper.getTitle() == null || paper.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Paper title khong duoc rong");
        }
    }
    
    public boolean addPaper(Paper paper) {
        validatePaper(paper);
        return service.addPaper(paper);
    }
    
    public Paper getPaper(String paperId) {
        validateId(paperId, "Paper");
        return service.getPaper(paperId);
    }
    
    public List<Paper> getAllPapers() {
        return service.getAllPapers();
    }
    
    public List<Paper> getPapersByStatus(com.uth.confms.pc.entity.enums.ReviewStatus status) {
        List<Paper> allPapers = service.getAllPapers();
        return allPapers.stream()
                .filter(p -> p.getReviewStatus() == status)
                .toList();
    }
}

