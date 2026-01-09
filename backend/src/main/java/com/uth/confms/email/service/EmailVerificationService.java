package com.uth.confms.email.service;

import com.uth.confms.auth.entity.EmailVerificationToken;
import com.uth.confms.auth.entity.User;
import com.uth.confms.auth.repository.EmailVerificationTokenRepository;
import com.uth.confms.auth.repository.UserRepository;
import com.uth.confms.common.exception.BusinessException;
import com.uth.confms.common.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý email verification
 *
 * <p>Service này xử lý các nghiệp vụ liên quan đến:
 *
 * <ul>
 *   <li>Gửi email verification token sử dụng EmailService với template
 *   <li>Verify email bằng token
 *   <li>Resend verification email
 *   <li>Quản lý token expiration
 * </ul>
 *
 * <p>Service này sử dụng EmailService để gửi emails với Thymeleaf templates.
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Service
@SuppressWarnings("null")
public class EmailVerificationService {
  private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
  
  private final EmailVerificationTokenRepository tokenRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;

  @Value("${app.email.verification.expiration-hours:24}")
  private int expirationHours;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public EmailVerificationService(
      EmailVerificationTokenRepository tokenRepository,
      UserRepository userRepository,
      EmailService emailService) {
    this.tokenRepository = tokenRepository;
    this.userRepository = userRepository;
    this.emailService = emailService;
  }

  /**
   * Gửi email verification cho user
   *
   * @param user User cần gửi verification email
   */
  @Transactional
  public void sendVerificationEmail(User user) {
    // Delete old token if exists
    tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

    // Create new token
    String token = UUID.randomUUID().toString();
    EmailVerificationToken verificationToken =
        EmailVerificationToken.builder()
            .token(token)
            .user(user)
            .expiresAt(LocalDateTime.now().plusHours(expirationHours))
            .used(false)
            .build();

    tokenRepository.save(verificationToken);

    // Prepare email template model
    Map<String, Object> model = new HashMap<>();
    model.put("userName", user.getFullName());
    model.put("verificationUrl", frontendUrl + "/verify-email?token=" + token);
    model.put("expirationHours", expirationHours);

    // Send email using EmailService with template
    // Don't throw exception if email sending fails (e.g., SMTP not configured)
    // Token is still created and saved, user can verify manually or resend email later
    try {
      emailService.sendEmail(user.getEmail(), "Xác thực Email - UTH-ConfMS", "verification", model);
      log.info("Verification email sent successfully to: {}", user.getEmail());
    } catch (Exception e) {
      log.warn("Failed to send verification email to: {}. Token created: {}. Error: {}", 
          user.getEmail(), token, e.getMessage());
      // Don't throw exception - allow registration to succeed even if email fails
      // User can still verify email later using the token
    }
  }

  /**
   * Verify email bằng token
   *
   * @param token Verification token
   * @throws NotFoundException Nếu token không tồn tại
   * @throws BusinessException Nếu token đã được sử dụng hoặc đã hết hạn
   */
  @Transactional
  public void verifyEmail(String token) {
    EmailVerificationToken verificationToken =
        tokenRepository
            .findByToken(token)
            .orElseThrow(() -> new NotFoundException("Invalid verification token"));

    if (verificationToken.getUsed()) {
      throw new BusinessException("Token already used", "TOKEN_USED");
    }

    if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new BusinessException("Token expired", "TOKEN_EXPIRED");
    }

    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    userRepository.save(user);

    verificationToken.setUsed(true);
    tokenRepository.save(verificationToken);
  }

  /**
   * Gửi lại verification email
   *
   * @param email Email của user cần gửi lại verification
   * @throws NotFoundException Nếu user không tồn tại
   * @throws BusinessException Nếu email đã được verify
   */
  @Transactional
  public void resendVerificationToken(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

    if (user.getEmailVerified()) {
      throw new BusinessException("Email already verified", "EMAIL_ALREADY_VERIFIED");
    }

    sendVerificationEmail(user);
  }
}
