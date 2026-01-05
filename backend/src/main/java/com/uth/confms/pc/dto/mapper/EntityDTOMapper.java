package com.uth.confms.pc.dto.mapper;

import com.uth.confms.pc.entity.*;
import com.uth.confms.pc.dto.*;

/**
 * Mapper class de chuyen doi giua Entity va DTO
 */
public class EntityDTOMapper {
    
    public static PCMemberDTO toDTO(PCMember member) {
        if (member == null) {
            return null;
        }
        
        PCMemberDTO dto = new PCMemberDTO();
        dto.setId(member.getId());
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setInstitution(member.getInstitution());
        dto.setResearchTopics(member.getResearchTopics());
        dto.setInvitationStatus(member.getInvitationStatus() != null ? member.getInvitationStatus().name() : null);
        dto.setAssignedPapersCount(member.getAssignedPapers() != null ? member.getAssignedPapers().size() : 0);
        return dto;
    }
    
    public static PaperDTO toDTO(Paper paper) {
        if (paper == null) {
            return null;
        }
        
        PaperDTO dto = new PaperDTO();
        dto.setId(paper.getId());
        dto.setTitle(paper.getTitle());
        dto.setAuthors(paper.getAuthors());
        dto.setAuthorInstitution(paper.getAuthorInstitution());
        dto.setKeywords(paper.getKeywords());
        dto.setReviewStatus(paper.getReviewStatus() != null ? paper.getReviewStatus().name() : null);
        dto.setAssignedReviewersCount(paper.getAssignedReviewers() != null ? paper.getAssignedReviewers().size() : 0);
        return dto;
    }
    
    public static ReviewDTO toDTO(Review review) {
        if (review == null) {
            return null;
        }
        
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setPcMemberId(review.getPcMemberId());
        dto.setPaperId(review.getPaperId());
        dto.setStatus(review.getStatus() != null ? review.getStatus().name() : null);
        dto.setScore(review.getScore());
        dto.setComment(review.getComment());
        return dto;
    }
    
    public static COIDTO toDTO(COI coi) {
        if (coi == null) {
            return null;
        }
        
        COIDTO dto = new COIDTO();
        dto.setId(coi.getId());
        dto.setPcMemberId(coi.getPcMemberId());
        dto.setPaperId(coi.getPaperId());
        dto.setType(coi.getType() != null ? coi.getType().name() : null);
        dto.setDescription(coi.getDescription());
        dto.setSource(coi.getSource() != null ? coi.getSource().name() : null);
        return dto;
    }
}

