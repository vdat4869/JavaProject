package com.uth.confms.pc.repository;

import com.uth.confms.pc.entity.Review;
import java.util.*;

/**
 * Repository quan ly viec luu tru va truy xuat Review entities
 */
public class ReviewRepository {
    private Map<String, Review> reviews;

    public ReviewRepository() {
        this.reviews = new HashMap<>();
    }

    public Review save(Review review) {
        if (review == null || review.getId() == null) {
            return null;
        }
        reviews.put(review.getId(), review);
        return review;
    }

    public Optional<Review> findById(String id) {
        return Optional.ofNullable(reviews.get(id));
    }

    public List<Review> findAll() {
        return new ArrayList<>(reviews.values());
    }

    public List<Review> findByPcMemberId(String pcMemberId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getPcMemberId().equals(pcMemberId)) {
                result.add(review);
            }
        }
        return result;
    }

    public List<Review> findByPaperId(String paperId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getPaperId().equals(paperId)) {
                result.add(review);
            }
        }
        return result;
    }

    public List<Review> findByStatus(com.uth.confms.pc.entity.enums.ReviewProgressStatus status) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getStatus() == status) {
                result.add(review);
            }
        }
        return result;
    }

    public boolean existsById(String id) {
        return reviews.containsKey(id);
    }

    public void deleteById(String id) {
        reviews.remove(id);
    }

    public void deleteAll() {
        reviews.clear();
    }
}

