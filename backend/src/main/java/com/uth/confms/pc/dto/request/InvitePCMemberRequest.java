package com.uth.confms.pc.dto.request;

import java.util.List;

/**
 * DTO request cho viec moi PC member
 */
public class InvitePCMemberRequest {
    private String id;
    private String name;
    private String email;
    private String institution;
    private List<String> researchTopics;
    private List<String> coAuthors;

    public InvitePCMemberRequest() {
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

    public List<String> getCoAuthors() {
        return coAuthors;
    }

    public void setCoAuthors(List<String> coAuthors) {
        this.coAuthors = coAuthors;
    }
}



