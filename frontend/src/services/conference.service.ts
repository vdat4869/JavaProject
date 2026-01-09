import apiClient from './api'

/**
 * Conference interface
 */
export interface Conference {
  id: number
  name: string
  description: string
  startDate: string
  endDate: string
  submissionDeadline: string
  reviewDeadline: string
  active: boolean
}

/**
 * CFP (Call for Papers) interface
 */
export interface CFP {
  conferenceId: number
  description: string
  topics: string[]
  tracks: Track[]
  deadlines: Deadline[]
}

/**
 * Track interface
 */
export interface Track {
  id: number
  name: string
  description: string
}

/**
 * Deadline interface
 */
export interface Deadline {
  type: 'SUBMISSION' | 'REVIEW' | 'CAMERA_READY'
  deadline: string
  description?: string
}

/**
 * Update Conference request
 */
export interface UpdateConferenceRequest {
  name?: string
  description?: string
  startDate?: string
  endDate?: string
  submissionDeadline?: string
  reviewDeadline?: string
  active?: boolean
}

/**
 * Conference Service - Xử lý các API calls liên quan đến conferences
 */
export const conferenceService = {
  /**
   * Lấy danh sách conferences
   * GET /api/conferences
   */
  getConferences: async (): Promise<Conference[]> => {
    const response = await apiClient.get<Conference[]>('/conferences')
    return response.data
  },

  /**
   * Lấy thông tin conference theo ID
   * GET /api/conferences/{id}
   */
  getConference: async (id: number): Promise<Conference> => {
    const response = await apiClient.get<Conference>(`/conferences/${id}`)
    return response.data
  },

  /**
   * Cập nhật conference (CHAIR only)
   * PUT /api/conferences/{id}
   */
  updateConference: async (id: number, data: UpdateConferenceRequest): Promise<Conference> => {
    const response = await apiClient.put<Conference>(`/conferences/${id}`, data)
    return response.data
  },

  /**
   * Lấy CFP của conference
   * GET /api/conferences/{id}/cfp
   */
  getCFP: async (id: number): Promise<CFP> => {
    const response = await apiClient.get<CFP>(`/conferences/${id}/cfp`)
    return response.data
  },

  /**
   * Cập nhật CFP (CHAIR only)
   * PUT /api/conferences/{id}/cfp
   */
  updateCFP: async (id: number, data: Partial<CFP>): Promise<CFP> => {
    const response = await apiClient.put<CFP>(`/conferences/${id}/cfp`, data)
    return response.data
  },
}
