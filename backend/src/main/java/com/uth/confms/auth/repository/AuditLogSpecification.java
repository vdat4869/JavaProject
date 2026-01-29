package com.uth.confms.auth.repository;

import com.uth.confms.auth.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AuditLogSpecification {

    public static Specification<AuditLog> withFilters(Long userId, String action, String resource, Long resourceId,
            LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            // System.out.println("DEBUG: Building predicate for AuditLogs. UserId: " +
            // userId);

            if (userId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("userId"), userId));
            }

            if (action != null && !action.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("action"), action));
            }

            if (resource != null && !resource.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("resource"), resource));
            }

            if (resourceId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("resourceId"), resourceId));
            }

            if (startDate != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }

            if (endDate != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }

            return predicate;
        };
    }
}
