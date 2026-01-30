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
  status: 'ASSIGNED' | 'ACCEPTED' | 'COMPLETED' | 'DECLINED'
  deadline: string
  canReview: boolean
  hasCOI: boolean
  reviewId?: number
}

/**
 * ReviewScore enum - matches backend ReviewScore enum
 */
export type ReviewScore =
  | 'STRONG_ACCEPT'
  | 'ACCEPT'
  | 'WEAK_ACCEPT'
  | 'BORDERLINE'
  | 'WEAK_REJECT'
  | 'REJECT'
  | 'STRONG_REJECT'

/**
 * Review interface - matches backend ReviewResponseDTO
 */
export interface Review {
  id: number
  assignmentId: number
  submissionId: number
  reviewerId: number
  reviewerName: string | null // Only visible to chair/admin, null for double-blind
  summary: string
  strengths?: string
  weaknesses?: string
  comments: string
  score: ReviewScore
  status: 'DRAFT' | 'SUBMITTED'
  isConfidential: boolean
  overallRating?: number // 1-5
  confidence?: number // 1-5
  numericScore?: number // 1-7
  createdAt: string
  submittedAt?: string
}

/**
 * Create/Update Review request - matches backend ReviewSubmitDTO
 */
export interface ReviewSubmitDTO {
  assignmentId: number
  summary: string
  strengths?: string
  weaknesses?: string
  comments: string
  score: ReviewScore
  isConfidential: boolean
  overallRating?: number // 1-5
  confidence?: number // 1-5
  templateId?: number
}

/**
 * Review Comment interface - matches backend ReviewCommentDTO
 */
export interface ReviewComment {
  id: number
  submissionId: number
  reviewerId: number
  reviewerName: string | null // Only visible to chair/admin for internal comments
  content: string
  isInternal: boolean
  createdAt: string
  updatedAt?: string
}

/**
 * Average Score interface - matches backend AverageScoreDTO
 */
export interface AverageScore {
  submissionId: number
  averageScore: number
  reviewCount: number
}

/**
 * Review Statistics interface - matches backend ReviewStatisticsDTO
 */
export interface ReviewStatistics {
  conferenceId: number
  completionRate: number // Percentage
  averageScore: number
  scoreDistribution: Record<string, number> // Score -> count
  averageCompletionTime: number // Days
  totalReviews: number
  completedReviews: number
  pendingReviews: number
  submissionTimeline: Record<string, number> // Date -> count
  reviewerMetrics: Record<number, ReviewerPerformance>
}

/**
 * Reviewer Performance interface - matches backend ReviewerPerformanceDTO
 */
export interface ReviewerPerformance {
  reviewerId: number
  reviewerName: string
  totalReviews: number
  completedReviews: number
  averageScore: number
  averageCompletionTime: number // Days
  completionRate: number // Percentage
}

/**
 * Rebuttal interface - matches backend RebuttalDTO
 */
export interface Rebuttal {
  id: number
  submissionId: number
  authorId: number
  content: string
  status: 'DRAFT' | 'SUBMITTED'
  createdAt: string
  submittedAt?: string
}

/**
 * Rebuttal Submit DTO - matches backend RebuttalSubmitDTO
 */
export interface RebuttalSubmitDTO {
  submissionId: number
  content: string
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
    const response = await apiClient.get<{ success: boolean; data: Assignment[] }>('/assignments/my')
    return response.data.data
  },

  /**
   * Lấy assignment theo ID
   * GET /api/assignments/{id}
   */
  getAssignment: async (id: number): Promise<Assignment> => {
    const response = await apiClient.get<{ success: boolean; data: Assignment }>(`/assignments/${id}`)
    return response.data.data
  },

  /**
   * Lấy review theo ID
   * GET /api/reviews/{id}
   */
  getReview: async (id: number): Promise<Review> => {
    const response = await apiClient.get<{ success: boolean; data: Review }>(`/reviews/${id}`)
    return response.data.data
  },

  /**
   * Lấy review theo assignment ID
   * GET /api/reviews/assignment/{assignmentId}
   */
  getReviewByAssignment: async (assignmentId: number): Promise<Review | null> => {
    try {
      const response = await apiClient.get<{ success: boolean; data: Review }>(
        `/reviews/assignment/${assignmentId}`
      )
      return response.data.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  },

  /**
   * Lấy tất cả reviews của submission
   * GET /api/reviews/submission/{submissionId}
   */
  getReviewsBySubmission: async (submissionId: number): Promise<Review[]> => {
    const response = await apiClient.get<{ success: boolean; data: Review[] }>(
      `/reviews/submission/${submissionId}`
    )
    return response.data.data
  },

  /**
   * Tạo hoặc cập nhật draft review
   * POST /api/reviews/draft
   */
  createOrUpdateDraft: async (data: ReviewSubmitDTO): Promise<Review> => {
    const response = await apiClient.post<{ success: boolean; data: Review }>(
      '/reviews/draft',
      data
    )
    return response.data.data
  },

  /**
   * Submit review (finalize)
   * POST /api/reviews/{id}/submit
   */
  submitReview: async (id: number): Promise<Review> => {
    const response = await apiClient.post<{ success: boolean; data: Review }>(
      `/reviews/${id}/submit`
    )
    return response.data.data
  },

  /**
   * Lấy average score của submission
   * GET /api/reviews/submission/{submissionId}/average-score
   */
  getAverageScore: async (submissionId: number): Promise<AverageScore> => {
    const response = await apiClient.get<{ success: boolean; data: AverageScore }>(
      `/reviews/submission/${submissionId}/average-score`
    )
    return response.data.data
  },

  /**
   * Lấy review statistics của conference
   * GET /api/reviews/conference/{conferenceId}/statistics
   */
  getReviewStatistics: async (conferenceId: number): Promise<ReviewStatistics> => {
    const response = await apiClient.get<{ success: boolean; data: ReviewStatistics }>(
      `/reviews/conference/${conferenceId}/statistics`
    )
    return response.data.data
  },

  /**
   * Lấy internal comments của submission
   * GET /api/reviews/submission/{submissionId}/comments
   */
  getInternalComments: async (submissionId: number): Promise<ReviewComment[]> => {
    const response = await apiClient.get<{ success: boolean; data: ReviewComment[] }>(
      `/reviews/submission/${submissionId}/comments`
    )
    return response.data.data
  },

  /**
   * Thêm internal comment vào submission
   * POST /api/reviews/submission/{submissionId}/comments
   * Note: Backend expects String content in request body, not JSON object
   */
  addInternalComment: async (submissionId: number, content: string): Promise<ReviewComment> => {
    const response = await apiClient.post<{ success: boolean; data: ReviewComment }>(
      `/reviews/submission/${submissionId}/comments`,
      content,
      {
        headers: {
          'Content-Type': 'text/plain',
        },
      }
    )
    return response.data.data
  },

  /**
   * Lấy rebuttal cho submission (nếu có)
   * GET /api/reviews/rebuttal/submission/{submissionId}
   */
  getRebuttal: async (submissionId: number): Promise<Rebuttal | null> => {
    try {
      const response = await apiClient.get<{ success: boolean; data: Rebuttal }>(
        `/reviews/rebuttal/submission/${submissionId}`
      )
      return response.data.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  },

  /**
   * Tạo hoặc cập nhật rebuttal draft
   * POST /api/reviews/rebuttal
   */
  createOrUpdateRebuttal: async (data: RebuttalSubmitDTO): Promise<Rebuttal> => {
    const response = await apiClient.post<{ success: boolean; data: Rebuttal }>(
      '/reviews/rebuttal',
      data
    )
    return response.data.data
  },

  /**
   * Submit rebuttal (finalize)
   * POST /api/reviews/rebuttal/{id}/submit
   */
  submitRebuttal: async (id: number): Promise<Rebuttal> => {
    const response = await apiClient.post<{ success: boolean; data: Rebuttal }>(
      `/reviews/rebuttal/${id}/submit`
    )
    return response.data.data
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
