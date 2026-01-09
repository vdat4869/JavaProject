package com.uth.confms.config;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Configuration cho Internationalization (i18n)
 *
 * <p>Configuration này cung cấp:
 *
 * <ul>
 *   <li>MessageSource: Load messages từ properties files
 *   <li>LocaleResolver: Resolve locale từ Accept-Language header
 *   <li>Supported locales: English (en) và Vietnamese (vi)
 * </ul>
 *
 * <p>Message files:
 *
 * <ul>
 *   <li>messages_en.properties: English messages
 *   <li>messages_vi.properties: Vietnamese messages
 * </ul>
 *
 * <p>Default locale: English (en)
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
@Configuration
@SuppressWarnings("null")
public class I18nConfig {

  /**
   * Tạo MessageSource để load messages từ properties files
   *
   * @return ResourceBundleMessageSource được cấu hình
   */
  @Bean
  public ResourceBundleMessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setUseCodeAsDefaultMessage(true); // Return code if message not found
    return messageSource;
  }

  /**
   * Tạo LocaleResolver để resolve locale từ HTTP request
   *
   * <p>Locale được resolve từ Accept-Language header. Supported locales: en (English), vi
   * (Vietnamese) Default locale: en
   *
   * @return LocaleResolver được cấu hình
   */
  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
    localeResolver.setDefaultLocale(Locale.ENGLISH);
    localeResolver.setSupportedLocales(
        Arrays.asList(
            Locale.ENGLISH, new Locale("vi") // Vietnamese
            ));
    return localeResolver;
  }
}
