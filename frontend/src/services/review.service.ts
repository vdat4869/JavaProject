import apiClient from './api'

/**
 * Assignment interface (Double-blind - không có author info)
 */
export interface Assignment {
  id: number
  submissionId: number
  submissionTitle: string
  submissionAbstract: string
  conferenceId: number
  conferenceName: string
  trackName?: string
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'DECLINED'
  deadline: string
  canReview: boolean
  hasCOI: boolean
  reviewId?: number
}

/**
 * Review interface
 */
export interface Review {
  id: number
  assignmentId: number
  submissionId: number
  overallRating: number
  confidence: number
  comments: string
  strengths: string
  weaknesses: string
  recommendation: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  status: 'DRAFT' | 'SUBMITTED'
  submittedAt?: string
  canEdit: boolean
}

/**
 * Create Review request
 */
export interface CreateReviewRequest {
  assignmentId: number
  overallRating: number
  confidence: number
  comments: string
  strengths: string
  weaknesses: string
  recommendation: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
}

/**
 * Update Review request
 */
export interface UpdateReviewRequest {
  overallRating?: number
  confidence?: number
  comments?: string
  strengths?: string
  weaknesses?: string
  recommendation?: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
}

/**
 * Discussion message interface
 */
export interface DiscussionMessage {
  id: number
  reviewId: number
  authorId: number
  authorName: string
  content: string
  createdAt: string
}

/**
 * Create Discussion message request
 */
export interface CreateDiscussionRequest {
  reviewId: number
  content: string
}

/**
 * Rebuttal interface (Double-blind - không có author info)
 */
export interface Rebuttal {
  id: number
  submissionId: number
  content: string
  submittedAt: string
}

/**
 * Review Service - Xử lý các API calls liên quan đến reviews
 */
export const reviewService = {
  /**
   * Lấy danh sách assignments (papers được giao)
   * GET /api/assignments
   */
  getAssignments: async (): Promise<Assignment[]> => {
    const response = await apiClient.get<Assignment[]>('/assignments')
    return response.data
  },

  /**
   * Lấy assignment theo ID
   * GET /api/assignments/{id}
   */
  getAssignment: async (id: number): Promise<Assignment> => {
    const response = await apiClient.get<Assignment>(`/assignments/${id}`)
    return response.data
  },

  /**
   * Lấy review theo ID
   * GET /api/reviews/{id}
   */
  getReview: async (id: number): Promise<Review> => {
    const response = await apiClient.get<Review>(`/reviews/${id}`)
    return response.data
  },

  /**
   * Tạo review mới
   * POST /api/reviews
   */
  createReview: async (data: CreateReviewRequest): Promise<Review> => {
    const response = await apiClient.post<Review>('/reviews', data)
    return response.data
  },

  /**
   * Cập nhật review
   * PUT /api/reviews/{id}
   */
  updateReview: async (id: number, data: UpdateReviewRequest): Promise<Review> => {
    const response = await apiClient.put<Review>(`/reviews/${id}`, data)
    return response.data
  },

  /**
   * Submit review (finalize)
   * POST /api/reviews/{id}/submit
   */
  submitReview: async (id: number): Promise<void> => {
    await apiClient.post(`/reviews/${id}/submit`)
  },

  /**
   * Lấy danh sách discussion messages
   * GET /api/reviews/{id}/discussion
   */
  getDiscussion: async (reviewId: number): Promise<DiscussionMessage[]> => {
    const response = await apiClient.get<DiscussionMessage[]>(`/reviews/${reviewId}/discussion`)
    return response.data
  },

  /**
   * Thêm message vào discussion
   * POST /api/reviews/{id}/discussion
   */
  addDiscussionMessage: async (data: CreateDiscussionRequest): Promise<DiscussionMessage> => {
    const response = await apiClient.post<DiscussionMessage>(
      `/reviews/${data.reviewId}/discussion`,
      { content: data.content },
    )
    return response.data
  },

  /**
   * Lấy rebuttal cho submission (nếu có)
   * GET /api/submissions/{id}/rebuttal
   */
  getRebuttal: async (submissionId: number): Promise<Rebuttal | null> => {
    try {
      const response = await apiClient.get<Rebuttal>(`/submissions/${submissionId}/rebuttal`)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  },

  /**
   * Download submission file (anonymized)
   * GET /api/assignments/{id}/file
   */
  downloadSubmissionFile: async (assignmentId: number): Promise<Blob> => {
    const response = await apiClient.get(`/assignments/${assignmentId}/file`, {
      responseType: 'blob',
    })
    return response.data
  },
}
