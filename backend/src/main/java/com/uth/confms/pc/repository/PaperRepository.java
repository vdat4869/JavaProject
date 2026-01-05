package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.Paper;
import java.util.*;

/**
 * Repository quan ly viec luu tru va truy xuat Paper entities
 */
public class PaperRepository {
    private Map<String, Paper> papers;

    public PaperRepository() {
        this.papers = new HashMap<>();
    }

    public Paper save(Paper paper) {
        if (paper == null || paper.getId() == null) {
            return null;
        }
        papers.put(paper.getId(), paper);
        return paper;
    }

    public Optional<Paper> findById(String id) {
        return Optional.ofNullable(papers.get(id));
    }

    public List<Paper> findAll() {
        return new ArrayList<>(papers.values());
    }

    public List<Paper> findByStatus(com.uth.confms.pc.entity.enums.ReviewStatus status) {
        List<Paper> result = new ArrayList<>();
        for (Paper paper : papers.values()) {
            if (paper.getReviewStatus() == status) {
                result.add(paper);
            }
        }
        return result;
    }

    public boolean existsById(String id) {
        return papers.containsKey(id);
    }

    public void deleteById(String id) {
        papers.remove(id);
    }

    public void deleteAll() {
        papers.clear();
    }
}

