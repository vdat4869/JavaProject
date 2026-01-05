package com.uth.confms.pc.dto.request;

import java.util.List;

/**
 * DTO request cho viec them paper
 */
public class AddPaperRequest {
    private String id;
    private String title;
    private List<String> authors;
    private String authorInstitution;
    private List<String> keywords;

    public AddPaperRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getAuthorInstitution() {
        return authorInstitution;
    }

    public void setAuthorInstitution(String authorInstitution) {
        this.authorInstitution = authorInstitution;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}



