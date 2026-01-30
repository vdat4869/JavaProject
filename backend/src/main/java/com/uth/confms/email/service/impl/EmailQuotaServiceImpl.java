package com.uth.confms.email.service.impl;

import com.uth.confms.email.entity.EmailQuota;
import com.uth.confms.email.repository.EmailQuotaRepository;
import com.uth.confms.email.service.EmailQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service để quản lý SMTP quota
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQuotaServiceImpl implements EmailQuotaService {

    private final EmailQuotaRepository quotaRepository;

    @Value("${app.email.quota.daily-limit:500}")
    private Long dailyQuotaLimit;

    @Value("${app.email.quota.hourly-limit:100}")
    private Long hourlyQuotaLimit;

    @Override
    @Transactional(readOnly = true)
    // Kiểm tra quota có khả dụng
    public boolean isQuotaAvailable(EmailQuota.QuotaType quotaType) {
        EmailQuota quota = getOrCreateQuota(quotaType);
        return !quota.isQuotaExceeded();
    }

    @Override
    @Transactional
    // Ghi nhận email đã gửi
    public boolean recordEmailSent(EmailQuota.QuotaType quotaType) {
        EmailQuota quota = getOrCreateQuota(quotaType);

        if (quota.isQuotaExceeded()) {
            log.warn("Quota exceeded for {}: {}/{}", quotaType, quota.getEmailsSent(), quota.getQuotaLimit());
            return false;
        }

        quota.setEmailsSent(quota.getEmailsSent() + 1);
        quota.setQuotaExceeded(quota.isQuotaExceeded());
        quota.setUpdatedAt(LocalDateTime.now());
        quotaRepository.save(quota);

        log.debug("Email quota updated for {}: {}/{}", quotaType, quota.getEmailsSent(), quota.getQuotaLimit());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    // Lấy quota còn lại
    public Long getRemainingQuota(EmailQuota.QuotaType quotaType) {
        EmailQuota quota = getOrCreateQuota(quotaType);
        return quota.getRemainingQuota();
    }

    @Override
    @Transactional(readOnly = true)
    // Lấy thông tin quota hiện tại
    public EmailQuota getCurrentQuota(EmailQuota.QuotaType quotaType) {
        return getOrCreateQuota(quotaType);
    }

    /**
     * Get or create quota record for today
     */
    private EmailQuota getOrCreateQuota(EmailQuota.QuotaType quotaType) {
        LocalDate today = LocalDate.now();

        return quotaRepository.findByQuotaDateAndQuotaType(today, quotaType)
                .orElseGet(() -> {
                    Long limit = quotaType == EmailQuota.QuotaType.DAILY ? dailyQuotaLimit : hourlyQuotaLimit;
                    EmailQuota newQuota = EmailQuota.builder()
                            .quotaDate(today)
                            .quotaType(quotaType)
                            .emailsSent(0L)
                            .quotaLimit(limit)
                            .quotaExceeded(false)
                            .build();
                    return quotaRepository.save(newQuota);
                });
    }
}
