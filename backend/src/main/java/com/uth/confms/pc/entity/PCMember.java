package com.uth.confms.pc.entity;

import com.uth.confms.pc.entity.enums.InvitationStatus;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity PCMember dai dien cho mot thanh vien trong Program Committee
 * Luu tru thong tin ca nhan, to chuc, va trang thai cua PC member
 */
public class PCMember {
    private String id;
    private String name;
    private String email;
    private String institution;
    private List<String> researchTopics;
    private InvitationStatus invitationStatus;
    private List<String> assignedPapers;
    private List<String> coAuthors;

    public PCMember() {
        this.researchTopics = new ArrayList<>();
        this.assignedPapers = new ArrayList<>();
        this.coAuthors = new ArrayList<>();
        this.invitationStatus = InvitationStatus.PENDING;
    }

    public PCMember(String id, String name, String email, String institution) {
        this();
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

    public void addResearchTopic(String topic) {
        if (!this.researchTopics.contains(topic)) {
            this.researchTopics.add(topic);
        }
    }

    public InvitationStatus getInvitationStatus() {
        return invitationStatus;
    }

    public void setInvitationStatus(InvitationStatus invitationStatus) {
        this.invitationStatus = invitationStatus;
    }

    public List<String> getAssignedPapers() {
        return assignedPapers;
    }

    public void setAssignedPapers(List<String> assignedPapers) {
        this.assignedPapers = assignedPapers;
    }

    public void assignPaper(String paperId) {
        if (!this.assignedPapers.contains(paperId)) {
            this.assignedPapers.add(paperId);
        }
    }

    public List<String> getCoAuthors() {
        return coAuthors;
    }

    public void setCoAuthors(List<String> coAuthors) {
        this.coAuthors = coAuthors;
    }

    public void addCoAuthor(String coAuthor) {
        if (!this.coAuthors.contains(coAuthor)) {
            this.coAuthors.add(coAuthor);
        }
    }

    public boolean isActive() {
        return invitationStatus == InvitationStatus.ACCEPTED;
    }

    @Override
    public String toString() {
        return "PCMember{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", institution='" + institution + '\'' +
                ", invitationStatus=" + invitationStatus +
                ", assignedPapers=" + assignedPapers.size() +
                '}';
    }
}

