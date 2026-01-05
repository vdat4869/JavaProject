package com.uth.confms.pc.service;

import com.uth.confms.pc.entity.*;
import com.uth.confms.pc.entity.enums.*;
import com.uth.confms.pc.repository.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service chinh quan ly toan bo hoat dong cua PC Management
 * Tich hop cac service khac de thuc hien cac chuc nang:
 * - Moi PC member
 * - Chap nhan/tu choi loi moi
 * - Assign reviewer (co kiem tra COI)
 * - Theo doi tien do review
 */
public class PCManagementService {
    
    private PCMemberRepository pcMemberRepository;
    private PaperRepository paperRepository;
    private ReviewRepository reviewRepository;
    private COIRepository coiRepository;
    
    private EmailService emailService;
    private COIDetectionService coiDetectionService;
    private ReviewerSuggestionService reviewerSuggestionService;
    
    private static final AtomicInteger reviewCounter = new AtomicInteger(1);
    
    private String generateReviewId() {
        return "REVIEW-" + reviewCounter.getAndIncrement();
    }
    
    public PCManagementService() {
        this.pcMemberRepository = new PCMemberRepository();
        this.paperRepository = new PaperRepository();
        this.reviewRepository = new ReviewRepository();
        this.coiRepository = new COIRepository();
        
        this.emailService = new EmailService();
        this.coiDetectionService = new COIDetectionService(coiRepository);
        this.reviewerSuggestionService = new ReviewerSuggestionService();
    }
    
    public boolean invitePCMember(PCMember pcMember) {
        if (pcMember == null || pcMember.getId() == null) {
            System.out.println("❌ Khong the moi: Thong tin PC member khong hop le");
            return false;
        }
        
        if (pcMemberRepository.existsById(pcMember.getId())) {
            System.out.println("⚠️  PC member da ton tai: " + pcMember.getId());
            return false;
        }
        
        boolean emailSent = emailService.sendInvitation(pcMember);
        
        if (emailSent) {
            pcMemberRepository.save(pcMember);
            System.out.println("✅ Da moi PC member: " + pcMember.getName() + " (" + pcMember.getEmail() + ")");
            return true;
        }
        
        return false;
    }
    
    public boolean acceptInvitation(String pcMemberId) {
        Optional<PCMember> optional = pcMemberRepository.findById(pcMemberId);
        
        if (optional.isEmpty()) {
            System.out.println("❌ Khong tim thay PC member: " + pcMemberId);
            return false;
        }
        
        PCMember pcMember = optional.get();
        pcMember.setInvitationStatus(InvitationStatus.ACCEPTED);
        pcMemberRepository.save(pcMember);
        
        emailService.sendAcceptanceConfirmation(pcMember);
        
        System.out.println("✅ PC member da chap nhan loi moi: " + pcMember.getName());
        return true;
    }
    
    public boolean rejectInvitation(String pcMemberId) {
        Optional<PCMember> optional = pcMemberRepository.findById(pcMemberId);
        
        if (optional.isEmpty()) {
            System.out.println("❌ Khong tim thay PC member: " + pcMemberId);
            return false;
        }
        
        PCMember pcMember = optional.get();
        pcMember.setInvitationStatus(InvitationStatus.REJECTED);
        pcMemberRepository.save(pcMember);
        
        emailService.sendRejectionNotification(pcMember);
        
        System.out.println("❌ PC member da tu choi loi moi: " + pcMember.getName());
        return true;
    }
    
    public COI declareCOI(String pcMemberId, String paperId, COIType type, String description) {
        return coiDetectionService.declareCOI(pcMemberId, paperId, type, description);
    }
    
    public boolean addPaper(Paper paper) {
        if (paper == null || paper.getId() == null) {
            System.out.println("❌ Khong the them paper: Thong tin khong hop le");
            return false;
        }
        
        if (paperRepository.existsById(paper.getId())) {
            System.out.println("⚠️  Paper da ton tai: " + paper.getId());
            return false;
        }
        
        paperRepository.save(paper);
        System.out.println("✅ Da them paper: " + paper.getTitle() + " (" + paper.getId() + ")");
        return true;
    }
    
    public boolean assignReviewer(String pcMemberId, String paperId) {
        Optional<PCMember> pcMemberOpt = pcMemberRepository.findById(pcMemberId);
        if (pcMemberOpt.isEmpty()) {
            System.out.println("❌ Khong tim thay PC member: " + pcMemberId);
            return false;
        }
        
        PCMember pcMember = pcMemberOpt.get();
        if (!pcMember.isActive()) {
            System.out.println("❌ PC member chua chap nhan loi moi: " + pcMember.getName());
            return false;
        }
        
        Optional<Paper> paperOpt = paperRepository.findById(paperId);
        if (paperOpt.isEmpty()) {
            System.out.println("❌ Khong tim thay paper: " + paperId);
            return false;
        }
        
        Paper paper = paperOpt.get();
        
        List<COI> existingCOIs = coiRepository.findByPcMemberId(pcMemberId);
        for (COI coi : existingCOIs) {
            if (coi.getPaperId().equals(paperId)) {
                System.out.println("🚫 KHONG THE ASSIGN - Phat hien COI:");
                System.out.println("   PC Member: " + pcMember.getName());
                System.out.println("   Paper: " + paper.getTitle());
                System.out.println("   Loai COI: " + coi.getType());
                System.out.println("   Mo ta: " + coi.getDescription());
                return false;
            }
        }
        
        List<COI> detectedCOIs = coiDetectionService.detectCOI(pcMember, paper);
        if (!detectedCOIs.isEmpty()) {
            System.out.println("🚫 KHONG THE ASSIGN - Phat hien COI tu dong:");
            for (COI coi : detectedCOIs) {
                System.out.println("   Loai COI: " + coi.getType());
                System.out.println("   Mo ta: " + coi.getDescription());
            }
            return false;
        }
        
        pcMember.assignPaper(paperId);
        paper.assignReviewer(pcMemberId);
        pcMemberRepository.save(pcMember);
        paperRepository.save(paper);
        
        String reviewId = generateReviewId();
        Review review = new Review(reviewId, pcMemberId, paperId);
        reviewRepository.save(review);
        
        paper.setReviewStatus(ReviewStatus.UNDER_REVIEW);
        paperRepository.save(paper);
        
        emailService.sendPaperAssignmentNotification(pcMember, paperId);
        
        System.out.println("✅ Da assign reviewer thanh cong:");
        System.out.println("   PC Member: " + pcMember.getName());
        System.out.println("   Paper: " + paper.getTitle());
        
        return true;
    }
    
    public boolean updateReviewProgress(String reviewId, ReviewProgressStatus status, Double score, String comment) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        
        if (reviewOpt.isEmpty()) {
            System.out.println("❌ Khong tim thay review: " + reviewId);
            return false;
        }
        
        Review review = reviewOpt.get();
        review.setStatus(status);
        
        if (status == ReviewProgressStatus.COMPLETED) {
            review.setScore(score);
            review.setComment(comment);
            
            Optional<Paper> paperOpt = paperRepository.findById(review.getPaperId());
            if (paperOpt.isPresent()) {
                Paper paper = paperOpt.get();
                boolean allCompleted = true;
                for (String reviewerId : paper.getAssignedReviewers()) {
                    List<Review> reviews = reviewRepository.findByPcMemberId(reviewerId);
                    boolean foundCompleted = false;
                    for (Review r : reviews) {
                        if (r.getPaperId().equals(paper.getId()) && r.isCompleted()) {
                            foundCompleted = true;
                            break;
                        }
                    }
                    if (!foundCompleted) {
                        allCompleted = false;
                        break;
                    }
                }
                
                if (allCompleted) {
                    paper.setReviewStatus(ReviewStatus.REVIEWED);
                    paperRepository.save(paper);
                    System.out.println("✅ Paper da hoan thanh tat ca reviews: " + paper.getTitle());
                }
            }
        }
        
        reviewRepository.save(review);
        
        System.out.println("✅ Da cap nhat tien do review: " + reviewId);
        System.out.println("   Trang thai: " + status);
        if (score != null) {
            System.out.println("   Diem so: " + score);
        }
        
        return true;
    }
    
    public List<Review> getReviewProgress(String pcMemberId) {
        return reviewRepository.findByPcMemberId(pcMemberId);
    }
    
    public List<PCMember> suggestReviewers(String paperId, int topN) {
        Optional<Paper> paperOpt = paperRepository.findById(paperId);
        
        if (paperOpt.isEmpty()) {
            System.out.println("❌ Khong tim thay paper: " + paperId);
            return new ArrayList<>();
        }
        
        Paper paper = paperOpt.get();
        List<PCMember> activeMembers = pcMemberRepository.findActiveMembers();
        
        List<PCMember> suggestions = reviewerSuggestionService.suggestReviewers(paper, activeMembers, topN);
        reviewerSuggestionService.printSuggestions(suggestions, paper);
        
        return suggestions;
    }
    
    public PCMember getPCMember(String pcMemberId) {
        return pcMemberRepository.findById(pcMemberId).orElse(null);
    }
    
    public Paper getPaper(String paperId) {
        return paperRepository.findById(paperId).orElse(null);
    }
    
    public List<PCMember> getAllPCMembers() {
        return pcMemberRepository.findAll();
    }
    
    public List<Paper> getAllPapers() {
        return paperRepository.findAll();
    }
    
    public List<COI> getAllCOIs() {
        return coiRepository.findAll();
    }
}
