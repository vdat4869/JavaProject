import apiClient from './api'

/**
 * Decision interface
 */
export interface Decision {
  id: number
  submissionId: number
  submissionTitle: string
  decision: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  comments?: string
  decidedAt: string
  decidedBy: string
  reviewCount: number
  averageRating: number
}

/**
 * Create Decision request
 */
export interface CreateDecisionRequest {
  submissionId: number
  decision: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  comments?: string
}

/**
 * Bulk Decision request
 */
export interface BulkDecisionRequest {
  submissionIds: number[]
  decision: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  comments?: string
}

/**
 * Decision Service - Xử lý các API calls liên quan đến decisions
 */
export const decisionService = {
  /**
   * Lấy danh sách submissions cần quyết định
   * GET /api/decisions/pending?conferenceId={id}
   */
  getPendingDecisions: async (conferenceId: number): Promise<Decision[]> => {
    const response = await apiClient.get<Decision[]>(
      `/decisions/pending?conferenceId=${conferenceId}`,
    )
    return response.data
  },

  /**
   * Lấy danh sách decisions đã quyết định
   * GET /api/decisions?conferenceId={id}
   */
  getDecisions: async (conferenceId: number): Promise<Decision[]> => {
    const response = await apiClient.get<Decision[]>(`/decisions?conferenceId=${conferenceId}`)
    return response.data
  },

  /**
   * Tạo decision cho submission
   * POST /api/decisions
   */
  createDecision: async (data: CreateDecisionRequest): Promise<Decision> => {
    const response = await apiClient.post<Decision>('/decisions', data)
    return response.data
  },

  /**
   * Bulk decisions (quyết định hàng loạt)
   * POST /api/decisions/bulk
   */
  bulkDecisions: async (data: BulkDecisionRequest): Promise<Decision[]> => {
    const response = await apiClient.post<Decision[]>('/decisions/bulk', data)
    return response.data
  },

  /**
   * Cập nhật decision
   * PUT /api/decisions/{id}
   */
  updateDecision: async (id: number, data: Partial<CreateDecisionRequest>): Promise<Decision> => {
    const response = await apiClient.put<Decision>(`/decisions/${id}`, data)
    return response.data
  },
}
