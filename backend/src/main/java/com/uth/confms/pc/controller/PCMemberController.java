package com.uth.confms.pc.controller;

import com.uth.confms.pc.entity.PCMember;
import com.uth.confms.pc.service.PCManagementService;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controller chuyen biet cho PCMember operations
 */
public class PCMemberController {
    
    private PCManagementService service;
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    public PCMemberController(PCManagementService service) {
        this.service = service;
    }
    
    private void validateId(String id, String entityName) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(entityName + " ID khong duoc rong");
        }
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return emailPattern.matcher(email).matches();
    }
    
    private void validatePCMember(PCMember member) {
        if (member == null) {
            throw new IllegalArgumentException("PCMember khong duoc null");
        }
        if (member.getId() == null || member.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("PCMember ID khong duoc rong");
        }
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("PCMember name khong duoc rong");
        }
        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("PCMember email khong duoc rong");
        }
        if (!isValidEmail(member.getEmail())) {
            throw new IllegalArgumentException("Email khong hop le: " + member.getEmail());
        }
    }
    
    public boolean invitePCMember(PCMember pcMember) {
        validatePCMember(pcMember);
        return service.invitePCMember(pcMember);
    }
    
    public boolean acceptInvitation(String pcMemberId) {
        validateId(pcMemberId, "PCMember");
        return service.acceptInvitation(pcMemberId);
    }
    
    public boolean rejectInvitation(String pcMemberId) {
        validateId(pcMemberId, "PCMember");
        return service.rejectInvitation(pcMemberId);
    }
    
    public PCMember getPCMember(String pcMemberId) {
        validateId(pcMemberId, "PCMember");
        return service.getPCMember(pcMemberId);
    }
    
    public List<PCMember> getAllPCMembers() {
        return service.getAllPCMembers();
    }
    
    public List<PCMember> getActiveMembers() {
        List<PCMember> allMembers = service.getAllPCMembers();
        return allMembers.stream()
                .filter(PCMember::isActive)
                .toList();
    }
}

