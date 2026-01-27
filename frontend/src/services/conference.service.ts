import apiClient from './api'

/**
 * Topic interface
 */
export interface Topic {
  id?: number
  name: string
  description?: string
}

/**
 * Keyword interface
 */
export interface Keyword {
  id: number
  name: string
  description?: string
}

/**
 * Track interface
 */
export interface Track {
  id?: number
  name: string
  description?: string
  active?: boolean
}

/**
 * Deadline interface
 */
export interface Deadline {
  id?: number
  type: 'SUBMISSION' | 'REVIEW' | 'CAMERA_READY'
  dueDate: string
  description?: string
  hardDeadline?: boolean
}

/**
 * CFP Response interface
 */
export interface CFPResponse {
  id: number
  callForPapers: string
  topics?: string // Deprecated
  topicsList?: Topic[]
  submissionGuidelines?: string
  open: boolean
  createdAt: string
  updatedAt: string
}

/**
 * Conference Response interface (matches backend ConferenceResponseDTO)
 */
export interface ConferenceResponse {
  id: number
  name: string
  acronym?: string
  description?: string
  chairId: number
  published: boolean
  reviewMode?: string
  topics?: Topic[]
  keywords?: Keyword[]
  tracks?: Track[]
  deadlines?: Deadline[]
  cfp?: CFPResponse
  createdAt: string
  updatedAt: string
}

/**
 * Conference Create DTO
 */
export interface ConferenceCreateRequest {
  name: string
  acronym?: string
  description?: string
  reviewMode?: string
  topics?: Topic[]
  keywordIds?: number[]
  tracks?: Track[]
  deadlines?: Deadline[]
  chairId?: number
}

/**
 * Conference Update DTO
 */
export interface ConferenceUpdateRequest {
  name?: string
  acronym?: string
  description?: string
  reviewMode?: string
  topics?: Topic[]
  keywordIds?: number[]
  tracks?: Track[]
  deadlines?: Deadline[]
  published?: boolean
}

/**
 * CFP DTO
 */
export interface CFPRequest {
  conferenceId: number
  callForPapers?: string
  topics?: string // Deprecated
  topicIds?: number[]
  submissionGuidelines?: string
  open?: boolean
}

/**
 * Conference Service - Xử lý các API calls liên quan đến conferences
 */
export const conferenceService = {
  /**
   * Lấy danh sách conferences đã publish (public)
   * GET /api/conferences/public
   */
  getPublishedConferences: async (): Promise<ConferenceResponse[]> => {
    const response = await apiClient.get<any>('/conferences/public')
    const data = response.data?.data || response.data
    return Array.isArray(data) ? data : []
  },

  /**
   * Alias for getPublishedConferences to match existing usage
   */
  getConferences: async (): Promise<ConferenceResponse[]> => {
    return conferenceService.getPublishedConferences()
  },

  /**
   * Lấy danh sách conferences của chair hiện tại
   * GET /api/conferences/my
   */
  getMyConferences: async (): Promise<ConferenceResponse[]> => {
    const response = await apiClient.get<any>('/conferences/my')
    const data = response.data?.data || response.data
    return Array.isArray(data) ? data : []
  },

  /**
   * Lấy thông tin conference theo ID
   * GET /api/conferences/{id}
   */
  getConference: async (id: number): Promise<ConferenceResponse> => {
    const response = await apiClient.get<any>(`/conferences/${id}`)
    return response.data?.data || response.data
  },

  /**
   * Tạo conference mới (CHAIR/ADMIN only)
   * POST /api/conferences
   */
  createConference: async (data: ConferenceCreateRequest): Promise<ConferenceResponse> => {
    const response = await apiClient.post<any>('/conferences', data)
    return response.data?.data || response.data
  },

  /**
   * Cập nhật conference (CHAIR/ADMIN only)
   * PUT /api/conferences/{id}
   */
  updateConference: async (
    id: number,
    data: ConferenceUpdateRequest,
  ): Promise<ConferenceResponse> => {
    const response = await apiClient.put<any>(`/conferences/${id}`, data)
    return response.data?.data || response.data
  },

  /**
   * Xóa conference (CHAIR/ADMIN only)
   * DELETE /api/conferences/{id}
   */
  deleteConference: async (id: number): Promise<void> => {
    await apiClient.delete(`/conferences/${id}`)
  },

  /**
   * Lấy CFP của conference
   * GET /api/cfp/conference/{conferenceId}
   */
  getCFP: async (conferenceId: number): Promise<CFPResponse> => {
    const response = await apiClient.get<any>(`/cfp/conference/${conferenceId}`)
    return response.data?.data || response.data
  },

  /**
   * Tạo hoặc cập nhật CFP (CHAIR/ADMIN only)
   * POST /api/cfp
   */
  createOrUpdateCFP: async (data: CFPRequest): Promise<CFPResponse> => {
    const response = await apiClient.post<any>('/cfp', data)
    return response.data?.data || response.data
  },

  /**
   * Publish CFP (CHAIR/ADMIN only)
   * POST /api/cfp/{conferenceId}/publish
   */
  publishCFP: async (conferenceId: number): Promise<CFPResponse> => {
    const response = await apiClient.post<any>(`/cfp/${conferenceId}/publish`)
    return response.data?.data || response.data
  },

  /**
   * Close CFP (CHAIR/ADMIN only)
   * POST /api/cfp/{conferenceId}/close
   */
  closeCFP: async (conferenceId: number): Promise<CFPResponse> => {
    const response = await apiClient.post<any>(`/cfp/${conferenceId}/close`)
    return response.data?.data || response.data
  },
}

// Legacy interfaces for backward compatibility
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

export interface CFP {
  conferenceId: number
  description: string
  topics: string[]
  tracks: Track[]
  deadlines: Deadline[]
}

export interface UpdateConferenceRequest {
  name?: string
  description?: string
  startDate?: string
  endDate?: string
  submissionDeadline?: string
  reviewDeadline?: string
  active?: boolean
}
