import apiClient from './api'

/**
 * Email Notification request
 */
export interface EmailNotificationRequest {
  recipientIds: number[]
  subject: string
  templateName: string
  templateData?: Record<string, any>
}

/**
 * Bulk Email Preview
 */
export interface BulkEmailPreview {
  recipientCount: number
  recipients: Array<{
    id: number
    email: string
    fullName: string
  }>
  subject: string
  preview: string
}

/**
 * Notification Service - Xử lý các API calls liên quan đến notifications
 */
export const notificationService = {
  /**
   * Preview bulk email (xem trước email hàng loạt)
   * POST /api/notifications/email/preview
   */
  previewBulkEmail: async (data: EmailNotificationRequest): Promise<BulkEmailPreview> => {
    const response = await apiClient.post<BulkEmailPreview>('/notifications/email/preview', data)
    return response.data
  },

  /**
   * Gửi bulk email
   * POST /api/notifications/email
   */
  sendBulkEmail: async (data: EmailNotificationRequest): Promise<void> => {
    await apiClient.post('/notifications/email', data)
  },
}
