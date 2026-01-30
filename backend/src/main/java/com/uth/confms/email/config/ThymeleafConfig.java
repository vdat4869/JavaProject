package com.uth.confms.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/**
 * Configuration cho Thymeleaf template engine cho email templates
 *
 * <p>
 * Configuration này tạo một TemplateEngine riêng cho email templates, tách biệt
 * với web
 * templates (nếu có).
 *
 * <p>
 * Email templates được lưu tại: resources/templates/email/
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Configuration
public class ThymeleafConfig {

  /**
   * Tạo TemplateEngine cho email templates
   *
   * @return TemplateEngine được cấu hình cho HTML email templates
   */
  @Bean(name = "emailTemplateEngine")
  // Cấu hình TemplateEngine cho email templates
  public TemplateEngine emailTemplateEngine() {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(emailTemplateResolver());
    return templateEngine;
  }

  /**
   * Tạo TemplateResolver cho email templates
   *
   * @return ITemplateResolver được cấu hình cho email templates
   */
  // Cấu hình TemplateResolver cho email templates
  private ITemplateResolver emailTemplateResolver() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setCacheable(false); // Disable cache for development
    return templateResolver;
  }
}
