package com.uth.confms.pc.dto;

/**
 * DTO cho COI - dung de truyen du lieu giua cac layer
 */
public class COIDTO {
    private String id;
    private String pcMemberId;
    private String paperId;
    private String type;
    private String description;
    private String source;

    public COIDTO() {
    }

    public COIDTO(String id, String pcMemberId, String paperId, String type, String description, String source) {
        this.id = id;
        this.pcMemberId = pcMemberId;
        this.paperId = paperId;
        this.type = type;
        this.description = description;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPcMemberId() {
        return pcMemberId;
    }

    public void setPcMemberId(String pcMemberId) {
        this.pcMemberId = pcMemberId;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}

