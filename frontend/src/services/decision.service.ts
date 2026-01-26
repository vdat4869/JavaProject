import apiClient from './api'

/**
 * Decision Type - khớp backend DecisionRequestDTO
 */
export type DecisionType = 'ACCEPT' | 'REJECT' | 'CONDITIONAL_ACCEPT'

/**
 * Review Summary interface - khớp backend ReviewSummaryDTO
 */
export interface ReviewSummary {
  submissionId: number
  averageScore: number
  reviewCount: number
  scoreDistribution: Record<string, number>
}

/**
 * Decision interface - khớp backend DecisionResultDTO
 */
export interface Decision {
  id: number
  submissionId: number
  submissionTitle: string
  decidedBy: number
  decidedByName: string
  type: DecisionType
  comments?: string
  notified: boolean
  locked: boolean
  decidedAt: string
  reviewSummary: ReviewSummary
}

/**
 * Create Decision request - khớp backend DecisionRequestDTO
 */
export interface CreateDecisionRequest {
  submissionId: number
  type: DecisionType
  comments?: string
  sendNotification?: boolean
}

/**
 * Bulk Notification Request - khớp backend BulkNotificationRequestDTO
 */
export interface BulkNotificationRequest {
  submissionIds: number[]
  notificationType: string // DECISION_ACCEPT, DECISION_REJECT
  customSubject?: string
  customMessage?: string
}

/**
 * Decision History interface
 */
export interface DecisionHistory {
  id: number
  decisionId: number
  changedBy: number
  changedByName: string
  changeType: string
  oldValue?: string
  newValue?: string
  fieldName?: string
  description?: string
  changedAt: string
}

/**
 * Bulk Decision Request
 */
export interface BulkDecisionRequest {
  submissionIds: number[]
  type: DecisionType
  comments?: string
  sendNotification?: boolean
}

/**
 * Update Decision request - khớp backend UpdateDecisionRequestDTO
 */
export interface UpdateDecisionRequest {
  type?: DecisionType
  comments?: string
  reason: string // Required for audit trail
}

/**
 * Decision Service - Xử lý các API calls liên quan đến decisions
 */
export const decisionService = {
  /**
   * Tạo decision cho submission
   * POST /api/decisions
   */
  createDecision: async (data: CreateDecisionRequest): Promise<Decision> => {
    const response = await apiClient.post<{ success: boolean; data: Decision }>(
      '/decisions',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Cập nhật decision (chỉ khi chưa notified/locked)
   * PUT /api/decisions/{decisionId}
   */
  updateDecision: async (decisionId: number, data: UpdateDecisionRequest): Promise<Decision> => {
    const response = await apiClient.put<{ success: boolean; data: Decision }>(
      `/decisions/${decisionId}`,
      data
    )
    return response.data.data || response.data
  },

  /**
   * Lấy decision theo submission
   * GET /api/decisions/submission/{submissionId}
   */
  getDecisionBySubmission: async (submissionId: number): Promise<Decision> => {
    const response = await apiClient.get<{ success: boolean; data: Decision }>(
      `/decisions/submission/${submissionId}`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy danh sách decisions theo conference
   * GET /api/decisions/conference/{conferenceId}
   */
  getDecisionsByConference: async (conferenceId: number): Promise<Decision[]> => {
    const response = await apiClient.get<{ success: boolean; data: Decision[] }>(
      `/decisions/conference/${conferenceId}`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy danh sách pending notifications
   * GET /api/decisions/pending-notifications
   */
  getPendingNotifications: async (): Promise<Decision[]> => {
    const response = await apiClient.get<{ success: boolean; data: Decision[] }>(
      '/decisions/pending-notifications'
    )
    return response.data.data || response.data
  },

  /**
   * Tạo bulk decisions
   * POST /api/decisions/bulk
   */
  createBulkDecisions: async (data: BulkDecisionRequest): Promise<Decision[]> => {
    const response = await apiClient.post<{ success: boolean; data: Decision[] }>(
      '/decisions/bulk',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Lấy lịch sử decision
   * GET /api/decisions/{id}/history
   */
  getDecisionHistory: async (decisionId: number): Promise<DecisionHistory[]> => {
    const response = await apiClient.get<{ success: boolean; data: DecisionHistory[] }>(
      `/decisions/${decisionId}/history`
    )
    return response.data.data || response.data
  },

  /**
   * Gửi notification cho decision
   * POST /api/decisions/notify/{decisionId}
   */
  sendNotification: async (decisionId: number): Promise<void> => {
    await apiClient.post(`/decisions/notify/${decisionId}`)
  },

  /**
   * Gửi bulk notifications
   * POST /api/decisions/notifications/bulk
   */
  sendBulkNotifications: async (data: BulkNotificationRequest): Promise<void> => {
    await apiClient.post('/decisions/notifications/bulk', data)
  },
}

