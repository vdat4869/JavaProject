package com.uth.confms.email.service;

import java.util.List;
import java.util.Map;

/**
 * Service interface cho email operations
 *
 * <p>
 * Service này cung cấp các operations để gửi email:
 *
 * <ul>
 * <li>Gửi email đơn lẻ với template
 * <li>Gửi bulk emails với template
 * <li>Hỗ trợ Thymeleaf templates
 * </ul>
 *
 * <p>
 * Templates được lưu tại: resources/templates/email/
 *
 * @author UTH-ConfMS Team
 * @version 1.0
 */
public interface EmailService {

  /**
   * Gửi email đơn lẻ sử dụng template
   *
   * @param to           Email người nhận
   * @param subject      Tiêu đề email
   * @param templateName Tên template (không bao gồm extension, ví dụ:
   *                     "verification" cho
   *                     "verification.html")
   * @param model        Map chứa các biến để render template
   * @throws RuntimeException Nếu có lỗi khi gửi email
   */
  // Gửi email đơn lẻ sử dụng template
  void sendEmail(String to, String subject, String templateName, Map<String, Object> model);

  /**
   * Gửi bulk emails sử dụng template (synchronous)
   *
   * <p>
   * Mỗi recipient sẽ nhận email với cùng template và model.
   * Process emails in batches với rate limiting và delays.
   *
   * @param recipients   Danh sách email người nhận
   * @param subject      Tiêu đề email
   * @param templateName Tên template (không bao gồm extension)
   * @param model        Map chứa các biến để render template
   * @return Số lượng email đã gửi thành công
   * @throws RuntimeException Nếu có lỗi khi gửi email
   */
  // Gửi danh sách emails đồng bộ (synchronous)
  int sendBulkEmail(
      List<String> recipients, String subject, String templateName, Map<String, Object> model);

  /**
   * Gửi bulk emails sử dụng template (asynchronous)
   *
   * <p>
   * Mỗi recipient sẽ nhận email với cùng template và model.
   * Process emails asynchronously với thread pool.
   *
   * @param recipients   Danh sách email người nhận
   * @param subject      Tiêu đề email
   * @param templateName Tên template (không bao gồm extension)
   * @param model        Map chứa các biến để render template
   * @return CompletableFuture với số lượng email đã gửi thành công
   */
  // Gửi danh sách emails bất đồng bộ (asynchronous)
  java.util.concurrent.CompletableFuture<Integer> sendBulkEmailAsync(
      List<String> recipients, String subject, String templateName, Map<String, Object> model);

  /**
   * Gửi email đơn giản (plain text, không dùng template)
   *
   * @param to      Email người nhận
   * @param subject Tiêu đề email
   * @param content Nội dung email (plain text)
   * @throws RuntimeException Nếu có lỗi khi gửi email
   */
  // Gửi email đơn giản (plain text)
  void sendSimpleEmail(String to, String subject, String content);
}
