import apiClient from './api'

/**
 * Assignment Statistics interface
 */
export interface AssignmentStats {
  totalSubmissions: number
  assignedSubmissions: number
  unassignedSubmissions: number
  completedReviews: number
  pendingReviews: number
  averageReviewsPerSubmission: number
}

/**
 * Auto Assignment request
 */
export interface AutoAssignmentRequest {
  conferenceId: number
  reviewsPerSubmission?: number
  considerCOI?: boolean
}

/**
 * Auto Assignment response
 */
export interface AutoAssignmentResponse {
  assignmentsCreated: number
  assignmentsUpdated: number
  conflictsResolved: number
}

/**
 * Manual Assignment request
 */
export interface ManualAssignmentRequest {
  submissionId: number
  reviewerIds: number[]
}

/**
 * Assignment Service - Xử lý các API calls liên quan đến assignments
 */
export const assignmentService = {
  /**
   * Lấy thống kê assignments
   * GET /api/assignments/stats?conferenceId={id}
   */
  getStats: async (conferenceId: number): Promise<AssignmentStats> => {
    const response = await apiClient.get<AssignmentStats>(
      `/assignments/stats?conferenceId=${conferenceId}`,
    )
    return response.data
  },

  /**
   * Auto assignment (tự động gán reviewers)
   * POST /api/assignments/auto
   */
  autoAssign: async (data: AutoAssignmentRequest): Promise<AutoAssignmentResponse> => {
    const response = await apiClient.post<AutoAssignmentResponse>('/assignments/auto', data)
    return response.data
  },

  /**
   * Manual assignment (gán thủ công)
   * POST /api/assignments/manual
   */
  manualAssign: async (data: ManualAssignmentRequest): Promise<void> => {
    await apiClient.post('/assignments/manual', data)
  },

  /**
   * Xóa assignment
   * DELETE /api/assignments/{id}
   */
  deleteAssignment: async (id: number): Promise<void> => {
    await apiClient.delete(`/assignments/${id}`)
  },
}
