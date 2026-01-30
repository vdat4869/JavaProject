package com.uth.confms.email.repository;

import com.uth.confms.email.entity.EmailQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmailQuotaRepository extends JpaRepository<EmailQuota, Long> {

    Optional<EmailQuota> findByQuotaDateAndQuotaType(LocalDate date, EmailQuota.QuotaType type);

    @Modifying
    @Query("UPDATE EmailQuota e SET e.emailsSent = e.emailsSent + 1, e.quotaExceeded = (e.emailsSent + 1 >= e.quotaLimit) WHERE e.quotaDate = :date AND e.quotaType = :type")
    // Tăng số lượng email đã gửi
    int incrementEmailCount(LocalDate date, EmailQuota.QuotaType type);

    @Query("SELECT SUM(e.emailsSent) FROM EmailQuota e WHERE e.quotaDate = :date")
    // Lấy tổng số email đã gửi trong ngày
    Long getTotalEmailsSentToday(LocalDate date);
}
