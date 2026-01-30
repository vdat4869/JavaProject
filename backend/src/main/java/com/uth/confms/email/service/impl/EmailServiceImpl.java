package com.uth.confms.email.service.impl;

import com.uth.confms.email.entity.EmailQueue;
import com.uth.confms.email.entity.EmailQuota;
import com.uth.confms.email.repository.EmailQueueRepository;
import com.uth.confms.email.service.EmailQuotaService;
import com.uth.confms.email.service.EmailRateLimiter;
import com.uth.confms.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation của EmailService sử dụng Thymeleaf templates
 * với retry mechanism và quota management
 *
 * <p>
 * Service này:
 *
 * <ul>
 * <li>Sử dụng Thymeleaf để render email templates
 * <li>Hỗ trợ HTML emails
 * <li>Gửi emails qua SMTP
 * <li>Retry mechanism với exponential backoff
 * <li>SMTP quota tracking và management
 * <li>Email queue cho failed emails
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 2.0
 */
@Service
@SuppressWarnings("null")
public class EmailServiceImpl implements EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;
  private final EmailQuotaService quotaService;
  private final EmailQueueRepository queueRepository;
  private final Executor emailTaskExecutor;
  private EmailRateLimiter rateLimiter;

  @Value("${spring.mail.username:}")
  private String fromEmail;

  @Value("${app.email.from-name:UTH-ConfMS}")
  private String fromName;

  @Value("${app.email.retry.max-attempts:3}")
  private int maxRetryAttempts;

  @Value("${app.email.async.batch-size:10}")
  private int batchSize;

  @Value("${app.email.async.batch-delay-ms:1000}")
  private long batchDelayMs;

  @Value("${app.email.rate-limit.max-per-minute:60}")
  private int maxEmailsPerMinute;

  public EmailServiceImpl(
      JavaMailSender mailSender,
      @Qualifier("emailTemplateEngine") TemplateEngine templateEngine,
      EmailQuotaService quotaService,
      EmailQueueRepository queueRepository,
      @Qualifier("emailTaskExecutor") Executor emailTaskExecutor) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
    this.quotaService = quotaService;
    this.queueRepository = queueRepository;
    this.emailTaskExecutor = emailTaskExecutor;
  }

  @jakarta.annotation.PostConstruct
  public void init() {
    this.rateLimiter = new EmailRateLimiter(maxEmailsPerMinute, 60000); // 60 seconds window
  }

  @Override
  @Retryable(retryFor = { MessagingException.class,
      RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
  // Gửi email đơn lẻ
  public void sendEmail(String to, String subject, String templateName, Map<String, Object> model) {
    // Check quota before sending
    if (!quotaService.isQuotaAvailable(EmailQuota.QuotaType.DAILY)) {
      log.warn("Daily quota exceeded. Queueing email to: {}", to);
      queueEmail(to, subject, templateName, null, model);
      throw new RuntimeException("Daily quota exceeded. Email queued for later retry.");
    }

    if (!quotaService.isQuotaAvailable(EmailQuota.QuotaType.HOURLY)) {
      log.warn("Hourly quota exceeded. Queueing email to: {}", to);
      queueEmail(to, subject, templateName, null, model);
      throw new RuntimeException("Hourly quota exceeded. Email queued for later retry.");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      // Set from
      if (fromEmail != null && !fromEmail.isEmpty()) {
        try {
          helper.setFrom(fromEmail, fromName != null ? fromName : "UTH-ConfMS");
        } catch (UnsupportedEncodingException e) {
          helper.setFrom(fromEmail);
        }
      }

      // Set to, subject
      helper.setTo(to != null ? to : "");
      helper.setSubject(subject != null ? subject : "");

      // Render template
      Context context = new Context();
      if (model != null) {
        context.setVariables(model);
      }
      String htmlContent = templateEngine.process("email/" + templateName, context);

      // Set content
      helper.setText(htmlContent, true);

      // Send
      mailSender.send(message);

      // Record email sent
      quotaService.recordEmailSent(EmailQuota.QuotaType.DAILY);
      quotaService.recordEmailSent(EmailQuota.QuotaType.HOURLY);

      log.info("Email sent successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("Error sending email to: {}", to, e);
      // Queue email for retry
      queueEmail(to, subject, templateName, null, model);
      throw new RuntimeException("Failed to send email to: " + to, e);
    }
  }

  @Override
  @Async("emailTaskExecutor")
  // Gửi danh sách emails bất đồng bộ
  public CompletableFuture<Integer> sendBulkEmailAsync(
      List<String> recipients, String subject, String templateName, Map<String, Object> model) {
    return CompletableFuture.supplyAsync(() -> {
      return sendBulkEmail(recipients, subject, templateName, model);
    }, emailTaskExecutor);
  }

  @Override
  // Gửi danh sách emails đồng bộ theo lô
  public int sendBulkEmail(
      List<String> recipients, String subject, String templateName, Map<String, Object> model) {
    log.info("Starting bulk email sending to {} recipients", recipients.size());

    int successCount = 0;
    int queuedCount = 0;
    int rateLimitedCount = 0;

    // Process in batches
    List<List<String>> batches = partitionList(recipients, batchSize);

    for (int i = 0; i < batches.size(); i++) {
      List<String> batch = batches.get(i);
      log.debug("Processing batch {}/{} ({} recipients)", i + 1, batches.size(), batch.size());

      // Check rate limit before processing batch
      if (!rateLimiter.canSend()) {
        long waitTime = rateLimiter.getTimeUntilReset();
        log.warn("Rate limit exceeded. Waiting {} ms before next batch", waitTime);
        try {
          Thread.sleep(waitTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("Interrupted while waiting for rate limit", e);
        }
      }

      // Process batch
      for (String recipient : batch) {
        try {
          // Check rate limit for each email
          if (!rateLimiter.canSend()) {
            rateLimitedCount++;
            log.debug("Rate limit reached, queueing email to: {}", recipient);
            queueEmail(recipient, subject, templateName, null, model);
            continue;
          }

          sendEmail(recipient, subject, templateName, model);
          rateLimiter.recordSent();
          successCount++;
        } catch (Exception e) {
          log.error("Failed to send email to: {}", recipient, e);
          queuedCount++;
          // Email is already queued in sendEmail method
        }
      }

      // Add delay between batches (except for last batch)
      if (i < batches.size() - 1 && batchDelayMs > 0) {
        try {
          Thread.sleep(batchDelayMs);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("Interrupted while waiting between batches", e);
        }
      }
    }

    log.info("Bulk email completed: {}/{} successful, {} queued, {} rate-limited",
        successCount, recipients.size(), queuedCount, rateLimitedCount);
    return successCount;
  }

  /**
   * Partition list into batches
   */
  private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
    List<List<T>> batches = new ArrayList<>();
    for (int i = 0; i < list.size(); i += batchSize) {
      batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
    }
    return batches;
  }

  @Override
  @Retryable(retryFor = { MessagingException.class,
      RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
  // Gửi email đơn giản (plain text)
  public void sendSimpleEmail(String to, String subject, String content) {
    // Check quota before sending
    if (!quotaService.isQuotaAvailable(EmailQuota.QuotaType.DAILY)) {
      log.warn("Daily quota exceeded. Queueing email to: {}", to);
      queueEmail(to, subject, null, content, null);
      throw new RuntimeException("Daily quota exceeded. Email queued for later retry.");
    }

    if (!quotaService.isQuotaAvailable(EmailQuota.QuotaType.HOURLY)) {
      log.warn("Hourly quota exceeded. Queueing email to: {}", to);
      queueEmail(to, subject, null, content, null);
      throw new RuntimeException("Hourly quota exceeded. Email queued for later retry.");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

      // Set from
      if (fromEmail != null && !fromEmail.isEmpty()) {
        try {
          helper.setFrom(fromEmail, fromName != null ? fromName : "UTH-ConfMS");
        } catch (UnsupportedEncodingException e) {
          helper.setFrom(fromEmail);
        }
      }

      // Set to, subject, content
      helper.setTo(to != null ? to : "");
      helper.setSubject(subject != null ? subject : "");
      helper.setText(content != null ? content : "", false);

      // Send
      mailSender.send(message);

      // Record email sent
      quotaService.recordEmailSent(EmailQuota.QuotaType.DAILY);
      quotaService.recordEmailSent(EmailQuota.QuotaType.HOURLY);

      log.info("Simple email sent successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("Error sending simple email to: {}", to, e);
      // Queue email for retry
      queueEmail(to, subject, null, content, null);
      throw new RuntimeException("Failed to send email to: " + to, e);
    }
  }

  /**
   * Queue email for retry
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  // Đưa email vào hàng đợi để retry sau
  public void queueEmail(String to, String subject, String templateName, String content, Map<String, Object> model) {
    try {
      // Render template if needed
      String renderedContent = content;
      if (templateName != null && model != null) {
        Context context = new Context();
        context.setVariables(model);
        renderedContent = templateEngine.process("email/" + templateName, context);
      }

      EmailQueue emailQueue = EmailQueue.builder()
          .recipient(to)
          .subject(subject)
          .templateName(templateName)
          .content(renderedContent)
          .status(EmailQueue.EmailStatus.PENDING)
          .retryCount(0)
          .maxRetries(maxRetryAttempts)
          .nextRetryAt(LocalDateTime.now().plusSeconds(1)) // Retry immediately
          .build();

      queueRepository.save(emailQueue);
      log.info("Email queued for retry: {}", to);
    } catch (Exception e) {
      log.error("Error queueing email: {}", to, e);
    }
  }
}
