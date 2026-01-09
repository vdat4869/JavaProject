import apiClient from './api'

/**
 * COI (Conflict of Interest) Declaration interface
 */
export interface COIDeclaration {
  id: number
  submissionId: number
  hasCOI: boolean
  reason?: string
  declaredAt: string
}

/**
 * Create COI Declaration request
 */
export interface CreateCOIRequest {
  submissionId: number
  hasCOI: boolean
  reason?: string
}

/**
 * PC Member interface
 */
export interface PCMember {
  id: number
  userId: number
  email: string
  fullName: string
  conferenceId: number
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED'
  invitedAt: string
  respondedAt?: string
}

/**
 * Invite PC Member request
 */
export interface InvitePCRequest {
  conferenceId: number
  email: string
  fullName?: string
}

/**
 * PC Service - Xử lý các API calls liên quan đến PC members
 */
export const pcService = {
  /**
   * Khai báo Conflict of Interest
   * POST /api/pc/coi
   */
  declareCOI: async (data: CreateCOIRequest): Promise<COIDeclaration> => {
    const response = await apiClient.post<COIDeclaration>('/pc/coi', data)
    return response.data
  },

  /**
   * Lấy COI declaration cho submission
   * GET /api/pc/coi/submission/{submissionId}
   */
  getCOI: async (submissionId: number): Promise<COIDeclaration | null> => {
    try {
      const response = await apiClient.get<COIDeclaration>(`/pc/coi/submission/${submissionId}`)
      return response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  },

  /**
   * Mời PC member
   * POST /api/pc/invite
   */
  invitePC: async (data: InvitePCRequest): Promise<PCMember> => {
    const response = await apiClient.post<PCMember>('/pc/invite', data)
    return response.data
  },

  /**
   * Lấy danh sách PC members của conference
   * GET /api/pc/conference/{conferenceId}
   */
  getPCMembers: async (conferenceId: number): Promise<PCMember[]> => {
    const response = await apiClient.get<PCMember[]>(`/pc/conference/${conferenceId}`)
    return response.data
  },
}
