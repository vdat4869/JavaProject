package com.uth.confms.email.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple rate limiter cho email sending
 */
public class EmailRateLimiter {

    private final AtomicInteger emailsSentInWindow = new AtomicInteger(0);
    private final AtomicLong windowStartTime = new AtomicLong(System.currentTimeMillis());
    private final int maxEmailsPerWindow;
    private final long windowSizeMs;

    public EmailRateLimiter(int maxEmailsPerWindow, long windowSizeMs) {
        this.maxEmailsPerWindow = maxEmailsPerWindow;
        this.windowSizeMs = windowSizeMs;
    }

    /**
     * Check if email can be sent (rate limit not exceeded)
     */
    // Kiểm tra xem có thể gửi email không (trong giới hạn rate limit)
    public synchronized boolean canSend() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();

        // Reset window if expired
        if (currentTime - windowStart >= windowSizeMs) {
            emailsSentInWindow.set(0);
            windowStartTime.set(currentTime);
            return true;
        }

        // Check if limit exceeded
        return emailsSentInWindow.get() < maxEmailsPerWindow;
    }

    /**
     * Record email sent
     */
    // Ghi nhận email đã gửi
    public synchronized void recordSent() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();

        // Reset window if expired
        if (currentTime - windowStart >= windowSizeMs) {
            emailsSentInWindow.set(1);
            windowStartTime.set(currentTime);
        } else {
            emailsSentInWindow.incrementAndGet();
        }
    }

    /**
     * Get remaining capacity in current window
     */
    // Lấy dung lượng còn lại trong cửa sổ thời gian
    public synchronized int getRemainingCapacity() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();

        // Reset window if expired
        if (currentTime - windowStart >= windowSizeMs) {
            return maxEmailsPerWindow;
        }

        return Math.max(0, maxEmailsPerWindow - emailsSentInWindow.get());
    }

    /**
     * Get time until next window reset (ms)
     */
    // Lấy thời gian còn lại đến khi reset window
    public synchronized long getTimeUntilReset() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        long elapsed = currentTime - windowStart;

        if (elapsed >= windowSizeMs) {
            return 0;
        }

        return windowSizeMs - elapsed;
    }
}
