package com.uth.confms.ai.repository;

import com.uth.confms.ai.entity.AIAuditLog;
import com.uth.confms.ai.enums.AIFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho AI Audit Log.
 */
@Repository
public interface AIAuditLogRepository extends JpaRepository<AIAuditLog, Long> {

    /** Lấy logs theo user */
    List<AIAuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /** Lấy logs theo conference */
    List<AIAuditLog> findByConferenceIdOrderByTimestampDesc(Long conferenceId);

    /** Lấy logs theo feature */
    List<AIAuditLog> findByAiFeatureOrderByTimestampDesc(AIFeature aiFeature);

    /** Lấy logs trong khoảng thời gian */
    List<AIAuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    /** Đếm số lần sử dụng theo feature trong conference */
    @Query("SELECT COUNT(a) FROM AIAuditLog a WHERE a.conferenceId = :conferenceId AND a.aiFeature = :feature")
    Long countByConferenceAndFeature(
            @Param("conferenceId") Long conferenceId,
            @Param("feature") AIFeature feature);

    /** Thống kê tokens sử dụng theo conference */
    @Query("SELECT SUM(a.tokensUsed) FROM AIAuditLog a WHERE a.conferenceId = :conferenceId")
    Long sumTokensByConference(@Param("conferenceId") Long conferenceId);
}
