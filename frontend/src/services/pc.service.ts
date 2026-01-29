import apiClient from './api'

/**
 * COI Type enum - matches backend ConflictOfInterest.COIType
 */
export type COIType = 'CO_AUTHOR' | 'COLLABORATOR' | 'ADVISOR' | 'INSTITUTIONAL' | 'OTHER'

/**
 * COI (Conflict of Interest) Declaration interface - matches backend ConflictOfInterest entity
 */
export interface COIDeclaration {
  id: number
  reviewerId: number
  submissionId: number
  type: COIType
  reason?: string
  active: boolean
  declaredAt: string
}

/**
 * Create COI Declaration request - matches backend COIDeclareDTO
 */
export interface CreateCOIRequest {
  submissionId: number
  type: COIType
  reason?: string
}

/**
 * PC Member interface - matches backend PCMemberDTO
 */
export interface PCMember {
  id: number
  conferenceId: number
  userId: number
  email: string
  fullName: string
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
  createdAt: string
  updatedAt: string
}

/**
 * PC Invitation interface - matches backend PCInvitationResponseDTO
 */
export interface PCInvitation {
  id: number
  conferenceId: number
  invitedUserEmail: string
  status: string
  expiresAt: string
  createdAt: string
}

/**
 * Invite PC Member request - matches backend PCInviteDTO
 */
export interface InvitePCRequest {
  conferenceId: number
  email: string
}

/**
 * Workload interface - matches backend WorkloadDTO
 */
export interface Workload {
  reviewerId: number
  reviewerEmail: string
  reviewerName: string
  conferenceId: number
  conferenceName: string
  totalAssignments: number
  assignedCount: number
  acceptedCount: number
  declinedCount: number
  completedCount: number
  workloadStatus: 'LOW' | 'NORMAL' | 'HIGH' | 'OVERLOADED'
  maxAssignments: number
  workloadPercentage: number
}

/**
 * Workload Statistics interface - matches backend WorkloadStatsDTO
 */
export interface WorkloadStats {
  conferenceId: number
  conferenceName: string
  totalReviewers: number
  totalAssignments: number
  averageAssignmentsPerReviewer: number
  lowWorkloadCount: number
  normalWorkloadCount: number
  highWorkloadCount: number
  overloadedCount: number
  assignedCount: number
  acceptedCount: number
  declinedCount: number
  completedCount: number
  reviewerWorkloads: Workload[]
}

/**
 * Workload Alert interface - matches backend WorkloadAlertDTO
 */
export interface WorkloadAlert {
  reviewerId: number
  reviewerEmail: string
  reviewerName: string
  conferenceId: number
  conferenceName: string
  currentAssignments: number
  maxAssignments: number
  workloadPercentage: number
  alertType: 'OVERLOADED' | 'NEAR_LIMIT'
  message: string
}

/**
 * COI History interface - matches backend COIHistoryDTO
 */
export interface COIHistory {
  id: number
  reviewerId: number
  reviewerEmail: string
  reviewerName: string
  submissionId: number
  submissionTitle: string
  coiType: COIType
  reason?: string
  active: boolean
  declaredAt: string
  action: 'DECLARED' | 'REMOVED' | 'AUTO_DETECTED'
}

/**
 * COI Statistics interface - matches backend COIStatisticsDTO
 */
export interface COIStatistics {
  conferenceId: number
  conferenceName: string
  totalCOIs: number
  activeCOIs: number
  inactiveCOIs: number
  coiByType: Record<string, number>
  reviewersWithCOIs: number
  submissionsWithCOIs: number
  recentCOIs: number
}

/**
 * PC Service - Xử lý các API calls liên quan đến PC members
 */
export const pcService = {
  /**
   * Khai báo Conflict of Interest
   * POST /api/pc/coi/declare
   */
  declareCOI: async (data: CreateCOIRequest): Promise<COIDeclaration> => {
    const response = await apiClient.post<{ success: boolean; data: COIDeclaration }>(
      '/pc/coi/declare',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Xóa COI declaration
   * DELETE /api/pc/coi/{coiId}
   */
  deleteCOI: async (coiId: number): Promise<void> => {
    await apiClient.delete(`/pc/coi/${coiId}`)
  },

  /**
   * Lấy danh sách COIs của reviewer hiện tại
   * GET /api/pc/coi/my
   */
  getMyCOIs: async (): Promise<COIDeclaration[]> => {
    const response = await apiClient.get<{ success: boolean; data: COIDeclaration[] }>('/pc/coi/my')
    return response.data.data || response.data
  },

  /**
   * Lấy danh sách COIs cho submission (for chairs)
   * GET /api/pc/coi/submission/{submissionId}
   */
  getCOIsBySubmission: async (submissionId: number): Promise<COIDeclaration[]> => {
    const response = await apiClient.get<{ success: boolean; data: COIDeclaration[] }>(
      `/pc/coi/submission/${submissionId}`
    )
    return response.data.data || response.data
  },

  /**
   * Kiểm tra COI cho submission
   * GET /api/pc/coi/check?submissionId={submissionId}
   */
  checkCOI: async (submissionId: number): Promise<boolean> => {
    const response = await apiClient.get<{ success: boolean; data: boolean }>(
      `/pc/coi/check?submissionId=${submissionId}`
    )
    return response.data.data ?? response.data
  },

  /**
   * Mời PC member
   * POST /api/pc/invite
   */
  invitePC: async (data: InvitePCRequest): Promise<PCInvitation> => {
    const response = await apiClient.post<{ success: boolean; data: PCInvitation }>(
      '/pc/invite',
      data
    )
    return response.data.data || response.data
  },

  /**
   * Chấp nhận invitation
   * POST /api/pc/invitation/accept?token={token}
   */
  acceptInvitation: async (token: string): Promise<PCMember> => {
    const response = await apiClient.post<{ success: boolean; data: PCMember }>(
      `/pc/invitation/accept?token=${encodeURIComponent(token)}`
    )
    return response.data.data || response.data
  },

  /**
   * Từ chối invitation
   * POST /api/pc/invitation/decline?token={token}
   */
  declineInvitation: async (token: string): Promise<void> => {
    await apiClient.post(`/pc/invitation/decline?token=${encodeURIComponent(token)}`)
  },

  /**
   * Lấy danh sách PC members của conference
   * GET /api/pc/conference/{conferenceId}/members
   */
  getPCMembers: async (conferenceId: number): Promise<PCMember[]> => {
    const response = await apiClient.get<{ success: boolean; data: PCMember[] }>(
      `/pc/conference/${conferenceId}/members`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy danh sách invitations của conference
   * GET /api/pc/conference/{conferenceId}/invitations
   */
  getInvitations: async (conferenceId: number): Promise<PCInvitation[]> => {
    const response = await apiClient.get<{ success: boolean; data: PCInvitation[] }>(
      `/pc/conference/${conferenceId}/invitations`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy workload của reviewer
   * GET /api/pc/reviewer/{reviewerId}/workload?conferenceId={conferenceId}
   */
  getReviewerWorkload: async (reviewerId: number, conferenceId: number): Promise<Workload> => {
    const response = await apiClient.get<{ success: boolean; data: Workload }>(
      `/pc/reviewer/${reviewerId}/workload`,
      {
        params: { conferenceId },
      }
    )
    return response.data.data || response.data
  },

  /**
   * Lấy workload statistics của conference
   * GET /api/pc/conference/{conferenceId}/workload-stats
   */
  getWorkloadStats: async (conferenceId: number): Promise<WorkloadStats> => {
    const response = await apiClient.get<{ success: boolean; data: WorkloadStats }>(
      `/pc/conference/${conferenceId}/workload-stats`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy workload alerts của conference
   * GET /api/pc/conference/{conferenceId}/workload-alerts
   */
  getWorkloadAlerts: async (conferenceId: number): Promise<WorkloadAlert[]> => {
    const response = await apiClient.get<{ success: boolean; data: WorkloadAlert[] }>(
      `/pc/conference/${conferenceId}/workload-alerts`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy COI history của conference
   * GET /api/pc/conference/{conferenceId}/coi/history
   */
  getCOIHistory: async (conferenceId: number): Promise<COIHistory[]> => {
    const response = await apiClient.get<{ success: boolean; data: COIHistory[] }>(
      `/pc/conference/${conferenceId}/coi/history`
    )
    return response.data.data || response.data
  },

  /**
   * Lấy COI statistics của conference
   * GET /api/pc/conference/{conferenceId}/coi/statistics
   */
  getCOIStatistics: async (conferenceId: number): Promise<COIStatistics> => {
    const response = await apiClient.get<{ success: boolean; data: COIStatistics }>(
      `/pc/conference/${conferenceId}/coi/statistics`
    )
    return response.data.data || response.data
  },
  /**
   * Lấy membership của user hiện tại trong conference
   * GET /api/pc/conference/{conferenceId}/membership
   */
  getMyMembership: async (conferenceId: number): Promise<PCMember | null> => {
    const response = await apiClient.get<{ success: boolean; data: PCMember }>(
      `/pc/conference/${conferenceId}/membership`
    )
    return response.data.data || response.data
  },
}
