import apiClient from './api'

/**
 * Assignment Status enum - matches backend AssignmentStatus enum
 */
export type AssignmentStatus = 'ASSIGNED' | 'ACCEPTED' | 'DECLINED' | 'COMPLETED'

/**
 * Assignment interface - matches backend AssignmentResponseDTO
 */
export interface Assignment {
  id: number
  submissionId: number
  submissionTitle: string
  reviewerId: number
  reviewerEmail: string
  reviewerName: string
  status: AssignmentStatus
  isPrimary: boolean
  assignedAt: string
  updatedAt: string
}

/**
 * Assignment Create DTO - matches backend AssignmentCreateDTO
 */
export interface AssignmentCreateDTO {
  submissionId: number
  reviewerId: number
  isPrimary?: boolean
}

/**
 * Assignment Suggestion interface - matches backend AssignmentSuggestionDTO
 */
export interface AssignmentSuggestion {
  reviewerId: number
  reviewerEmail: string
  reviewerName: string
  score: number // 0.0 - 1.0
  reason: string
  hasCOI: boolean
}

/**
 * Auto Assign Request DTO - matches backend AutoAssignRequestDTO
 */
export interface AutoAssignRequestDTO {
  submissionId: number
  numberOfReviewers?: number // Default: 3, min: 1
}

/**
 * Failed Assignment DTO - matches backend AutoAssignResponseDTO.FailedAssignmentDTO
 */
export interface FailedAssignment {
  reviewerId: number
  reviewerEmail: string
  reviewerName: string
  reason: string
}

/**
 * Auto Assign Response DTO - matches backend AutoAssignResponseDTO
 */
export interface AutoAssignResponse {
  createdAssignments: Assignment[]
  failedAssignments: FailedAssignment[]
  totalRequested: number
  totalCreated: number
  totalFailed: number
}

/**
 * Bulk Assign Request DTO - matches backend BulkAssignRequestDTO
 */
export interface BulkAssignRequestDTO {
  assignments: AssignmentCreateDTO[]
}

/**
 * Bulk Assign Response DTO - matches backend BulkAssignResponseDTO
 */
export interface BulkAssignResponse {
  createdAssignments: Assignment[]
  failedAssignments: BulkFailedAssignment[]
  totalRequested: number
  totalCreated: number
  totalFailed: number
}

/**
 * Bulk Failed Assignment DTO - matches backend BulkAssignResponseDTO.FailedAssignmentDTO
 */
export interface BulkFailedAssignment {
  submissionId: number
  reviewerId: number
  reason: string
}

/**
 * Reassign Request DTO - matches backend ReassignRequestDTO
 */
export interface ReassignRequestDTO {
  newReviewerId: number
  reason?: string
}

/**
 * Assignment Statistics DTO - matches backend AssignmentStatisticsDTO
 */
export interface AssignmentStatistics {
  totalAssignments: number
  totalReviewers: number
  averageAssignmentsPerReviewer: number
  minAssignments: number
  maxAssignments: number
  statusDistribution: Record<string, number> // ASSIGNED, ACCEPTED, DECLINED, COMPLETED -> count
  workloadDistribution: Record<string, number> // LOW, NORMAL, HIGH, OVERLOADED -> count
  acceptanceRate: number // Percentage
  completionRate: number // Percentage
  declineRate: number // Percentage
}

/**
 * Assignment Quality Metrics DTO - matches backend AssignmentQualityMetricsDTO
 */
export interface AssignmentQualityMetrics {
  averageReviewScore: number // 0.0 - 7.0
  reviewScoreDistribution: Record<string, number> // Score -> count
  averageReviewCompletionTime: number // Days
  totalReviewsSubmitted: number
  totalReviewsPending: number
  reviewSubmissionRate: number // Percentage
  averageReviewerRating: number
}

/**
 * Assignment Service - Xử lý các API calls liên quan đến assignments
 */
export const assignmentService = {
  /**
   * Tạo assignment mới (manual)
   * POST /api/assignments
   */
  createAssignment: async (data: AssignmentCreateDTO): Promise<Assignment> => {
    const response = await apiClient.post<{ success: boolean; data: Assignment }>(
      '/assignments',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Bulk assign reviewers
   * POST /api/assignments/bulk
   */
  bulkAssign: async (data: BulkAssignRequestDTO): Promise<BulkAssignResponse> => {
    const response = await apiClient.post<{ success: boolean; data: BulkAssignResponse }>(
      '/assignments/bulk',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Auto assign reviewers based on suggestions
   * POST /api/assignments/auto-assign
   */
  autoAssign: async (data: AutoAssignRequestDTO): Promise<AutoAssignResponse> => {
    const response = await apiClient.post<{ success: boolean; data: AutoAssignResponse }>(
      '/assignments/auto-assign',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Lấy suggestions cho submission
   * GET /api/assignments/submission/{submissionId}/suggestions
   */
  getSuggestions: async (submissionId: number): Promise<AssignmentSuggestion[]> => {
    const response = await apiClient.get<{ success: boolean; data: AssignmentSuggestion[] }>(
      `/assignments/submission/${submissionId}/suggestions`
    )
    return response.data.data || response.data
  },

  /**
   * Accept assignment
   * POST /api/assignments/{id}/accept
   */
  acceptAssignment: async (id: number): Promise<Assignment> => {
    const response = await apiClient.post<{ success: boolean; data: Assignment }>(
      `/assignments/${id}/accept`
    )
    return response.data.data || response.data
  },

  /**
   * Decline assignment
   * POST /api/assignments/{id}/decline
   */
  declineAssignment: async (id: number): Promise<Assignment> => {
    const response = await apiClient.post<{ success: boolean; data: Assignment }>(
      `/assignments/${id}/decline`
    )
    return response.data.data || response.data
  },

  /**
   * Reassign assignment
   * PUT /api/assignments/{id}/reassign
   */
  reassignAssignment: async (id: number, data: ReassignRequestDTO): Promise<Assignment> => {
    const response = await apiClient.put<{ success: boolean; data: Assignment }>(
      `/assignments/${id}/reassign`,
      data
    )
    return response.data.data || response.data
  },

  /**
   * Xóa assignment
   * DELETE /api/assignments/{id}
   */
  deleteAssignment: async (id: number): Promise<void> => {
    await apiClient.delete(`/assignments/${id}`)
  },

  /**
   * Lấy assignments của submission (chair/admin)
   * GET /api/assignments/submission/{submissionId}
   */
  getAssignmentsBySubmission: async (submissionId: number): Promise<Assignment[]> => {
    const response = await apiClient.get<{ success: boolean; data: Assignment[] }>(
      `/assignments/submission/${submissionId}`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy assignments của reviewer hiện tại
   * GET /api/assignments/my
   */
  getMyAssignments: async (): Promise<Assignment[]> => {
    const response = await apiClient.get<{ success: boolean; data: Assignment[] }>(
      '/assignments/my'
    )
    return response.data.data || response.data
  },

  /**
   * Lấy assignment theo ID
   * GET /api/assignments/{id}
   */
  getAssignment: async (id: number): Promise<Assignment> => {
    const response = await apiClient.get<{ success: boolean; data: Assignment }>(
      `/assignments/${id}`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy assignment statistics của conference
   * GET /api/assignments/conference/{conferenceId}/statistics
   */
  getAssignmentStatistics: async (conferenceId: number): Promise<AssignmentStatistics> => {
    const response = await apiClient.get<{ success: boolean; data: AssignmentStatistics }>(
      `/assignments/conference/${conferenceId}/statistics`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy assignment quality metrics của conference
   * GET /api/assignments/conference/{conferenceId}/quality-metrics
   */
  getAssignmentQualityMetrics: async (
    conferenceId: number
  ): Promise<AssignmentQualityMetrics> => {
    const response = await apiClient.get<{ success: boolean; data: AssignmentQualityMetrics }>(
      `/assignments/conference/${conferenceId}/quality-metrics`
    )
    return response.data.data || response.data
  },
}
