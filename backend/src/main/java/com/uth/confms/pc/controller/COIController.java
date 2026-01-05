package com.uth.confms.pc.controller;

import com.uth.confms.pc.entity.COI;
import com.uth.confms.pc.entity.enums.COIType;
import com.uth.confms.pc.service.PCManagementService;
import java.util.List;

/**
 * Controller chuyen biet cho COI operations
 */
public class COIController {
    
    private PCManagementService service;

    public COIController(PCManagementService service) {
        this.service = service;
    }
    
    private void validateId(String id, String entityName) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(entityName + " ID khong duoc rong");
        }
    }
    
    public COI declareCOI(String pcMemberId, String paperId, COIType type, String description) {
        validateId(pcMemberId, "PCMember");
        validateId(paperId, "Paper");
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mo ta COI khong duoc rong");
        }
        return service.declareCOI(pcMemberId, paperId, type, description);
    }
    
    public List<COI> getAllCOIs() {
        return service.getAllCOIs();
    }
    
    public List<COI> getCOIsByPCMember(String pcMemberId) {
        validateId(pcMemberId, "PCMember");
        return service.getAllCOIs().stream()
                .filter(coi -> coi.getPcMemberId().equals(pcMemberId))
                .toList();
    }
    
    public List<COI> getCOIsByPaper(String paperId) {
        validateId(paperId, "Paper");
        return service.getAllCOIs().stream()
                .filter(coi -> coi.getPaperId().equals(paperId))
                .toList();
    }
    
    public boolean hasCOI(String pcMemberId, String paperId) {
        validateId(pcMemberId, "PCMember");
        validateId(paperId, "Paper");
        return service.getAllCOIs().stream()
                .anyMatch(coi -> coi.getPcMemberId().equals(pcMemberId) && coi.getPaperId().equals(paperId));
    }
}

