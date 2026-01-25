package com.uth.confms.email.service;

import com.uth.confms.email.entity.EmailQuota;

/**
 * Service để quản lý SMTP quota
 */
public interface EmailQuotaService {
    
    /**
     * Check if quota is available
     *
     * @param quotaType Type of quota (DAILY or HOURLY)
     * @return true if quota is available, false if exceeded
     */
    boolean isQuotaAvailable(EmailQuota.QuotaType quotaType);
    
    /**
     * Record email sent (increment quota counter)
     *
     * @param quotaType Type of quota
     * @return true if email was recorded successfully, false if quota exceeded
     */
    boolean recordEmailSent(EmailQuota.QuotaType quotaType);
    
    /**
     * Get remaining quota
     *
     * @param quotaType Type of quota
     * @return Remaining quota count
     */
    Long getRemainingQuota(EmailQuota.QuotaType quotaType);
    
    /**
     * Get current quota usage
     *
     * @param quotaType Type of quota
     * @return Current quota usage
     */
    EmailQuota getCurrentQuota(EmailQuota.QuotaType quotaType);
}
