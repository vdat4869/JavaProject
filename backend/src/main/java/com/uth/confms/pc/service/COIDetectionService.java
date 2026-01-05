package com.uth.confms.pc.service;

import com.uth.confms.pc.entity.*;
import com.uth.confms.pc.entity.enums.*;
import com.uth.confms.pc.repository.COIRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service phat hien Conflict of Interest (COI)
 * Kiem tra 2 loai COI chinh:
 * 1. Cung co quan (Same Institution)
 * 2. Dong tac gia (Co-author)
 */
public class COIDetectionService {
    
    private COIRepository coiRepository;
    private static final AtomicInteger coiCounter = new AtomicInteger(1);

    public COIDetectionService(COIRepository coiRepository) {
        this.coiRepository = coiRepository;
    }
    
    private String generateCOIId() {
        return "COI-" + coiCounter.getAndIncrement();
    }
    
    public List<COI> detectCOI(PCMember pcMember, Paper paper) {
        List<COI> cois = new ArrayList<>();
        
        if (pcMember == null || paper == null) {
            return cois;
        }
        
        COI sameInstitutionCOI = checkSameInstitution(pcMember, paper);
        if (sameInstitutionCOI != null) {
            cois.add(sameInstitutionCOI);
            coiRepository.save(sameInstitutionCOI);
        }
        
        COI coAuthorCOI = checkCoAuthor(pcMember, paper);
        if (coAuthorCOI != null) {
            cois.add(coAuthorCOI);
            coiRepository.save(coAuthorCOI);
        }
        
        return cois;
    }
    
    private COI checkSameInstitution(PCMember pcMember, Paper paper) {
        String pcInstitution = pcMember.getInstitution();
        String paperInstitution = paper.getAuthorInstitution();
        
        if (pcInstitution != null && paperInstitution != null) {
            if (pcInstitution.trim().equalsIgnoreCase(paperInstitution.trim())) {
                String coiId = generateCOIId();
                String description = "Cung co quan: " + pcInstitution;
                
                COI coi = new COI(coiId, pcMember.getId(), paper.getId(), 
                                 COIType.SAME_INSTITUTION, description, COISource.DETECTED);
                
                System.out.println("⚠️  Phat hien COI cung co quan:");
                System.out.println("   PC Member: " + pcMember.getName() + " (" + pcInstitution + ")");
                System.out.println("   Paper: " + paper.getTitle() + " (" + paperInstitution + ")");
                
                return coi;
            }
        }
        
        return null;
    }
    
    private COI checkCoAuthor(PCMember pcMember, Paper paper) {
        List<String> pcCoAuthors = pcMember.getCoAuthors();
        List<String> paperAuthors = paper.getAuthors();
        
        for (String paperAuthor : paperAuthors) {
            for (String coAuthor : pcCoAuthors) {
                if (paperAuthor.trim().equalsIgnoreCase(coAuthor.trim())) {
                    String coiId = generateCOIId();
                    String description = "Dong tac gia voi: " + paperAuthor;
                    
                    COI coi = new COI(coiId, pcMember.getId(), paper.getId(), 
                                     COIType.CO_AUTHOR, description, COISource.DETECTED);
                    
                    System.out.println("⚠️  Phat hien COI dong tac gia:");
                    System.out.println("   PC Member: " + pcMember.getName());
                    System.out.println("   Paper: " + paper.getTitle());
                    System.out.println("   Dong tac gia: " + paperAuthor);
                    
                    return coi;
                }
            }
        }
        
        return null;
    }
    
    public COI declareCOI(String pcMemberId, String paperId, COIType type, String description) {
        String coiId = generateCOIId();
        
        COI coi = new COI(coiId, pcMemberId, paperId, type, description, COISource.DECLARED);
        coiRepository.save(coi);
        
        System.out.println("📝 COI da duoc khai bao thu cong:");
        System.out.println("   PC Member ID: " + pcMemberId);
        System.out.println("   Paper ID: " + paperId);
        System.out.println("   Loai: " + type);
        System.out.println("   Mo ta: " + description);
        
        return coi;
    }
    
    public boolean hasCOI(String pcMemberId, String paperId) {
        return coiRepository.existsByPcMemberAndPaper(pcMemberId, paperId);
    }
    
    public List<COI> getAllCOIs() {
        return coiRepository.findAll();
    }
    
    public List<COI> getCOIsByPCMember(String pcMemberId) {
        return coiRepository.findByPcMemberId(pcMemberId);
    }
    
    public List<COI> getCOIsByPaper(String paperId) {
        return coiRepository.findByPaperId(paperId);
    }
}
