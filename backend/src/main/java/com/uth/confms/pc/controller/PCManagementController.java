package com.uth.confms.pc.controller;

import com.uth.confms.pc.entity.*;
import com.uth.confms.pc.entity.enums.*;
import com.uth.confms.pc.service.PCManagementService;
import java.util.List;

/**
 * Controller quan ly PC Management
 * Xu ly cac request va goi service tuong ung
 */
public class PCManagementController {
    
    private PCManagementService service;

    public PCManagementController(PCManagementService service) {
        this.service = service;
    }
    
    public boolean invitePCMember(PCMember pcMember) {
        return service.invitePCMember(pcMember);
    }
    
    public boolean acceptInvitation(String pcMemberId) {
        return service.acceptInvitation(pcMemberId);
    }
    
    public boolean rejectInvitation(String pcMemberId) {
        return service.rejectInvitation(pcMemberId);
    }
    
    public COI declareCOI(String pcMemberId, String paperId, COIType type, String description) {
        return service.declareCOI(pcMemberId, paperId, type, description);
    }
    
    public boolean addPaper(Paper paper) {
        return service.addPaper(paper);
    }
    
    public boolean assignReviewer(String pcMemberId, String paperId) {
        return service.assignReviewer(pcMemberId, paperId);
    }
    
    public boolean updateReviewProgress(String reviewId, ReviewProgressStatus status, Double score, String comment) {
        return service.updateReviewProgress(reviewId, status, score, comment);
    }
    
    public List<Review> getReviewProgress(String pcMemberId) {
        return service.getReviewProgress(pcMemberId);
    }
    
    public List<PCMember> suggestReviewers(String paperId, int topN) {
        return service.suggestReviewers(paperId, topN);
    }
    
    public PCMember getPCMember(String pcMemberId) {
        return service.getPCMember(pcMemberId);
    }
    
    public Paper getPaper(String paperId) {
        return service.getPaper(paperId);
    }
    
    public List<PCMember> getAllPCMembers() {
        return service.getAllPCMembers();
    }
    
    public List<Paper> getAllPapers() {
        return service.getAllPapers();
    }
    
    public List<COI> getAllCOIs() {
        return service.getAllCOIs();
    }
}

