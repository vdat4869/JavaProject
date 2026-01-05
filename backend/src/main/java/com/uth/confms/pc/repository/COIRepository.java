package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.COI;
import java.util.*;

/**
 * Repository quan ly viec luu tru va truy xuat COI entities
 */
public class COIRepository {
    private Map<String, COI> cois;

    public COIRepository() {
        this.cois = new HashMap<>();
    }

    public COI save(COI coi) {
        if (coi == null || coi.getId() == null) {
            return null;
        }
        cois.put(coi.getId(), coi);
        return coi;
    }

    public Optional<COI> findById(String id) {
        return Optional.ofNullable(cois.get(id));
    }

    public List<COI> findAll() {
        return new ArrayList<>(cois.values());
    }

    public List<COI> findByPcMemberId(String pcMemberId) {
        List<COI> result = new ArrayList<>();
        for (COI coi : cois.values()) {
            if (coi.getPcMemberId().equals(pcMemberId)) {
                result.add(coi);
            }
        }
        return result;
    }

    public List<COI> findByPaperId(String paperId) {
        List<COI> result = new ArrayList<>();
        for (COI coi : cois.values()) {
            if (coi.getPaperId().equals(paperId)) {
                result.add(coi);
            }
        }
        return result;
    }

    public boolean existsByPcMemberAndPaper(String pcMemberId, String paperId) {
        for (COI coi : cois.values()) {
            if (coi.getPcMemberId().equals(pcMemberId) && coi.getPaperId().equals(paperId)) {
                return true;
            }
        }
        return false;
    }

    public void deleteById(String id) {
        cois.remove(id);
    }

    public void deleteAll() {
        cois.clear();
    }
}

