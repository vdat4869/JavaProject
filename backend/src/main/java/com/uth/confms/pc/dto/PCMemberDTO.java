package com.uth.confms.pc.dto;

import java.util.List;

/**
 * DTO cho PCMember - dung de truyen du lieu giua cac layer
 */
public class PCMemberDTO {
    private String id;
    private String name;
    private String email;
    private String institution;
    private List<String> researchTopics;
    private String invitationStatus;
    private int assignedPapersCount;

    public PCMemberDTO() {
    }

    public PCMemberDTO(String id, String name, String email, String institution) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.institution = institution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public List<String> getResearchTopics() {
        return researchTopics;
    }

    public void setResearchTopics(List<String> researchTopics) {
        this.researchTopics = researchTopics;
    }

    public String getInvitationStatus() {
        return invitationStatus;
    }

    public void setInvitationStatus(String invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public int getAssignedPapersCount() {
        return assignedPapersCount;
    }

    public void setAssignedPapersCount(int assignedPapersCount) {
        this.assignedPapersCount = assignedPapersCount;
    }
}

