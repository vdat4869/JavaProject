import apiClient from './api'

/**
 * Submission interface
 */
export interface Submission {
  id: number
  title: string
  abstract: string
  keywords: string[]
  conferenceId: number
  conferenceName?: string
  trackId?: number
  trackName?: string
  status:
    | 'DRAFT'
    | 'SUBMITTED'
    | 'UNDER_REVIEW'
    | 'REVIEWED'
    | 'ACCEPTED'
    | 'REJECTED'
    | 'WITHDRAWN'
    | 'CAMERA_READY'
  submittedAt?: string
  deadline?: string
  canEdit: boolean
  canWithdraw: boolean
  fileUrl?: string
  fileName?: string
}

/**
 * Create Submission request
 */
export interface CreateSubmissionRequest {
  title: string
  abstract: string
  keywords: string[]
  conferenceId: number
  trackId?: number
  file: File
}

/**
 * Update Submission request
 */
export interface UpdateSubmissionRequest {
  title?: string
  abstract?: string
  keywords?: string[]
  trackId?: number
  file?: File
}

/**
 * Review interface (anonymized)
 */
export interface Review {
  id: number
  submissionId: number
  overallRating: number
  confidence: number
  comments: string
  strengths: string
  weaknesses: string
  recommendation: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  submittedAt: string
  // Reviewer identity không được hiển thị
}

/**
 * Decision interface
 */
export interface Decision {
  id: number
  submissionId: number
  decision: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  comments?: string
  decidedAt: string
  decidedBy?: string
}

/**
 * Submission Service - Xử lý các API calls liên quan đến submissions
 */
export const submissionService = {
  /**
   * Lấy danh sách submissions của user hiện tại
   * GET /api/submissions/my-submissions
   */
  getMySubmissions: async (): Promise<Submission[]> => {
    const response = await apiClient.get<Submission[]>('/submissions/my-submissions')
    return response.data
  },

  /**
   * Lấy thông tin submission theo ID
   * GET /api/submissions/{id}
   */
  getSubmission: async (id: number): Promise<Submission> => {
    const response = await apiClient.get<Submission>(`/submissions/${id}`)
    return response.data
  },

  /**
   * Tạo submission mới
   * POST /api/submissions
   */
  createSubmission: async (data: CreateSubmissionRequest): Promise<Submission> => {
    const formData = new FormData()
    formData.append('title', data.title)
    formData.append('abstract', data.abstract)
    formData.append('keywords', JSON.stringify(data.keywords))
    formData.append('conferenceId', data.conferenceId.toString())
    if (data.trackId) {
      formData.append('trackId', data.trackId.toString())
    }
    formData.append('file', data.file)

    const response = await apiClient.post<Submission>('/submissions', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Cập nhật submission
   * PUT /api/submissions/{id}
   */
  updateSubmission: async (id: number, data: UpdateSubmissionRequest): Promise<Submission> => {
    const formData = new FormData()
    if (data.title) formData.append('title', data.title)
    if (data.abstract) formData.append('abstract', data.abstract)
    if (data.keywords) formData.append('keywords', JSON.stringify(data.keywords))
    if (data.trackId) formData.append('trackId', data.trackId.toString())
    if (data.file) formData.append('file', data.file)

    const response = await apiClient.put<Submission>(`/submissions/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Xóa/Withdraw submission
   * DELETE /api/submissions/{id}
   */
  deleteSubmission: async (id: number): Promise<void> => {
    await apiClient.delete(`/submissions/${id}`)
  },

  /**
   * Lấy danh sách reviews cho submission (anonymized)
   * GET /api/submissions/{id}/reviews
   */
  getReviews: async (id: number): Promise<Review[]> => {
    const response = await apiClient.get<Review[]>(`/submissions/${id}/reviews`)
    return response.data
  },

  /**
   * Lấy decision cho submission
   * GET /api/submissions/{id}/decision
   */
  getDecision: async (id: number): Promise<Decision | null> => {
    try {
      const response = await apiClient.get<Decision>(`/submissions/${id}/decision`)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null // Chưa có decision
      }
      throw error
    }
  },

  /**
   * Download submission file
   * GET /api/submissions/{id}/file
   */
  downloadFile: async (id: number): Promise<Blob> => {
    const response = await apiClient.get(`/submissions/${id}/file`, {
      responseType: 'blob',
    })
    return response.data
  },
}
