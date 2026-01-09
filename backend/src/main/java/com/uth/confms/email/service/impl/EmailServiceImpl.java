package com.uth.confms.email.service.impl;

import com.uth.confms.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementation của EmailService sử dụng Thymeleaf templates
 *
 * <p>Service này:
 *
 * <ul>
 *   <li>Sử dụng Thymeleaf để render email templates
 *   <li>Hỗ trợ HTML emails
 *   <li>Gửi emails qua SMTP
 *   <li>Templates được lưu tại: resources/templates/email/
 * </ul>
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class EmailServiceImpl implements EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username:}")
  private String fromEmail;

  @Value("${app.email.from-name:UTH-ConfMS}")
  private String fromName;

  public EmailServiceImpl(
      JavaMailSender mailSender, @Qualifier("emailTemplateEngine") TemplateEngine templateEngine) {
    this.mailSender = mailSender;
    this.templateEngine = templateEngine;
  }

  @Override
  public void sendEmail(String to, String subject, String templateName, Map<String, Object> model) {
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
      log.info("Email sent successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("Error sending email to: {}", to, e);
      throw new RuntimeException("Failed to send email to: " + to, e);
    }
  }

  @Override
  public int sendBulkEmail(
      List<String> recipients, String subject, String templateName, Map<String, Object> model) {
    int successCount = 0;
    for (String recipient : recipients) {
      try {
        sendEmail(recipient, subject, templateName, model);
        successCount++;
      } catch (Exception e) {
        log.error("Failed to send email to: {}", recipient, e);
        // Continue with other recipients
      }
    }
    log.info("Bulk email sent: {}/{} successful", successCount, recipients.size());
    return successCount;
  }

  @Override
  public void sendSimpleEmail(String to, String subject, String content) {
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
      log.info("Simple email sent successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("Error sending simple email to: {}", to, e);
      throw new RuntimeException("Failed to send email to: " + to, e);
    }
  }
}
