package com.uth.confms.pc.entity;

import com.uth.confms.pc.entity.enums.COIType;
import com.uth.confms.pc.entity.enums.COISource;

/**
 * Entity COI (Conflict of Interest) dai dien cho mot xung dot loi ich
 * Luu tru thong tin ve PC member va paper co xung dot
 */
public class COI {
    private String id;
    private String pcMemberId;
    private String paperId;
    private COIType type;
    private String description;
    private COISource source;

    public COI() {
    }

    public COI(String id, String pcMemberId, String paperId, COIType type, String description, COISource source) {
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

    public COIType getType() {
        return type;
    }

    public void setType(COIType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public COISource getSource() {
        return source;
    }

    public void setSource(COISource source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "COI{" +
                "id='" + id + '\'' +
                ", pcMemberId='" + pcMemberId + '\'' +
                ", paperId='" + paperId + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", source=" + source +
                '}';
    }
}

