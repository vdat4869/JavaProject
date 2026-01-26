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
 * Notification Log status and types
 */
export enum NotificationStatus {
  SENT = 'SENT',
  FAILED = 'FAILED',
  PENDING = 'PENDING',
}

export enum NotificationType {
  DECISION_ACCEPT = 'DECISION_ACCEPT',
  DECISION_REJECT = 'DECISION_REJECT',
  DECISION_CONDITIONAL_ACCEPT = 'DECISION_CONDITIONAL_ACCEPT',
  REVIEW_REQUEST = 'REVIEW_REQUEST',
  DEADLINE_REMINDER = 'DEADLINE_REMINDER',
}

export interface NotificationLog {
  id: number
  submissionId?: number
  userId: number
  type: NotificationType
  subject: string
  content: string
  sentAt: string
  status: NotificationStatus
}

/**
 * Notification Service - Xử lý các API calls liên quan đến notifications
 */
export const notificationService = {
  /**
   * Lấy tất cả nhật ký thông báo
   * GET /api/notifications
   */
  getLogs: async (): Promise<NotificationLog[]> => {
    const response = await apiClient.get<{ success: boolean; data: NotificationLog[] }>('/notifications')
    return response.data.data
  },

  /**
   * Lấy nhật ký thông báo theo submission
   * GET /api/notifications/submission/{id}
   */
  getLogsBySubmission: async (submissionId: number): Promise<NotificationLog[]> => {
    const response = await apiClient.get<{ success: boolean; data: NotificationLog[] }>(
      `/notifications/submission/${submissionId}`
    )
    return response.data.data
  },

  /**
   * Preview bulk email (xem trước email hàng loạt)
   * POST /api/notifications/email/preview (Legacy - not in backend yet)
   */
  previewBulkEmail: async (data: EmailNotificationRequest): Promise<BulkEmailPreview> => {
    const response = await apiClient.post<BulkEmailPreview>('/notifications/email/preview', data)
    return response.data
  },

  /**
   * Gửi bulk email (Legacy - not in backend yet)
   * POST /api/notifications/email
   */
  sendBulkEmail: async (data: EmailNotificationRequest): Promise<void> => {
    await apiClient.post('/notifications/email', data)
  },
}
