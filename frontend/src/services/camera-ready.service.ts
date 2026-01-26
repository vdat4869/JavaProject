import apiClient from './api'

/**
 * Camera-ready Status
 */
export type CameraReadyStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'NEEDS_REVISION'

/**
 * Version User interface
 */
export interface VersionUser {
  id: string
  fullName: string
  email: string
}

/**
 * Version interface - matches backend VersionDTO
 */
export interface CameraReadyVersion {
  id: string
  submissionId: string
  versionNumber: number
  originalFilename: string
  fileSizeBytes: number
  checksumSha256: string
  pageCount?: number
  pageSize?: string
  validationPassed: boolean
  uploadedBy: VersionUser
  uploadedAt: string
  isCurrent: boolean
}

/**
 * Submission interface - matches backend SubmissionDTO
 */
export interface CameraReadySubmission {
  id: string
  paperId: string
  paperTitle: string
  conferenceId: string
  trackId: string
  trackName: string
  status: CameraReadyStatus
  currentVersion?: CameraReadyVersion
  copyrightConfirmed: boolean
  copyrightConfirmedAt?: string
  deadline: string
  canUpload: boolean
  canConfirmCopyright: boolean
  createdAt: string
  updatedAt: string
}

/**
 * Copyright confirmation request
 */
export interface CopyrightConfirmRequest {
  confirmed: boolean
  ipAddress?: string
  userAgent?: string
}

/**
 * Chair Submission List Item
 */
export interface CameraReadySubmissionListItem {
  id: string
  paperId: string
  paperTitle: string
  trackId: string
  trackName: string
  status: CameraReadyStatus
  currentVersionNumber: number
  copyrightConfirmed: boolean
  correspondingAuthor: VersionUser
  updatedAt: string
}

/**
 * Camera-ready Statistics
 */
export interface CameraReadyStatistics {
  conferenceId: string
  deadline: string
  daysRemaining: number
  totalAcceptedPapers: number
  statistics: {
    byStatus: Record<string, number>
    copyrightConfirmed: number
    copyrightPending: number
    submissionRate: number
    approvalRate: number
  }
  byTrack: Array<{
    trackId: string
    trackName: string
    total: number
    submitted: number
    approved: number
  }>
}

/**
 * Review Request
 */
export type ReviewDecision = 'APPROVE' | 'REJECT' | 'NEEDS_REVISION'

export interface ReviewRequest {
  decision: ReviewDecision
  note?: string
  versionId?: string
}

/**
 * Camera-ready Service - API v1
 */
export const cameraReadyService = {
  /**
   * Lấy thông tin bài nộp camera-ready
   * GET /api/v1/conferences/{confId}/camera-ready/papers/{paperId}
   */
  getSubmission: async (conferenceId: string, paperId: string): Promise<CameraReadySubmission> => {
    const response = await apiClient.get<CameraReadySubmission>(
      `/v1/conferences/${conferenceId}/camera-ready/papers/${paperId}`
    )
    return response.data
  },

  /**
   * Upload phiên bản mới
   * POST /api/v1/conferences/{confId}/camera-ready/papers/{paperId}/upload
   */
  uploadVersion: async (
    conferenceId: string,
    paperId: string,
    file: File
  ): Promise<CameraReadyVersion> => {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post<CameraReadyVersion>(
      `/v1/conferences/${conferenceId}/camera-ready/papers/${paperId}/upload`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
    return response.data
  },

  /**
   * Danh sách phiên bản
   * GET /api/v1/conferences/{confId}/camera-ready/papers/{paperId}/versions
   */
  listVersions: async (conferenceId: string, paperId: string): Promise<CameraReadyVersion[]> => {
    const response = await apiClient.get<CameraReadyVersion[]>(
      `/v1/conferences/${conferenceId}/camera-ready/papers/${paperId}/versions`
    )
    return response.data
  },

  /**
   * Tải xuống một phiên bản
   * GET /api/v1/conferences/{confId}/camera-ready/papers/{paperId}/versions/{versionId}/download
   */
  downloadVersion: async (
    conferenceId: string,
    paperId: string,
    versionId: string
  ): Promise<Blob> => {
    const response = await apiClient.get(
      `/v1/conferences/${conferenceId}/camera-ready/papers/${paperId}/versions/${versionId}/download`,
      { responseType: 'blob' }
    )
    return response.data
  },

  /**
   * Xác nhận bản quyền
   * POST /api/v1/conferences/{confId}/camera-ready/papers/{paperId}/confirm-copyright
   */
  confirmCopyright: async (
    conferenceId: string,
    paperId: string,
    confirmed: boolean
  ): Promise<CameraReadySubmission> => {
    const request: CopyrightConfirmRequest = { confirmed }
    const response = await apiClient.post<CameraReadySubmission>(
      `/v1/conferences/${conferenceId}/camera-ready/papers/${paperId}/confirm-copyright`,
      request
    )
    return response.data
  },

  /**
   * [CHAIR] Danh sách tất cả bài nộp camera-ready
   * GET /api/v1/conferences/{confId}/camera-ready/submissions
   */
  listSubmissions: async (conferenceId: string): Promise<CameraReadySubmissionListItem[]> => {
    const response = await apiClient.get<CameraReadySubmissionListItem[]>(
      `/v1/conferences/${conferenceId}/camera-ready/submissions`
    )
    return response.data
  },

  /**
   * [CHAIR] Phê duyệt/Từ chối bài nộp
   * POST /api/v1/conferences/{confId}/camera-ready/submissions/{submissionId}/review
   */
  reviewSubmission: async (
    conferenceId: string,
    submissionId: string,
    review: ReviewRequest
  ): Promise<void> => {
    await apiClient.post(
      `/v1/conferences/${conferenceId}/camera-ready/submissions/${submissionId}/review`,
      review
    )
  },

  /**
   * [CHAIR] Lấy thống kê
   * GET /api/v1/conferences/{confId}/camera-ready/statistics
   */
  getStatistics: async (conferenceId: string): Promise<CameraReadyStatistics> => {
    const response = await apiClient.get<CameraReadyStatistics>(
      `/v1/conferences/${conferenceId}/camera-ready/statistics`
    )
    return response.data
  },

  /**
   * [CHAIR] Xuất bản kỷ yếu (ZIP/PDF/JSON/CSV)
   * GET /api/v1/conferences/{confId}/camera-ready/export/{format}
   */
  exportProceedings: async (conferenceId: string, format: 'zip' | 'pdf' | 'json' | 'csv'): Promise<Blob> => {
    const response = await apiClient.get(
      `/v1/conferences/${conferenceId}/camera-ready/export/${format}`,
      { responseType: 'blob' }
    )
    return response.data
  },

  // Legacy compatibility / CHAIR helpers
  getBySubmissionId: async (submissionId: number): Promise<any> => {
    // Lưu ý: Backend mới dùng UUID và PaperId, đây là shim để không crash code cũ nếu chưa refactor hết
    console.warn('Deprecated: use getSubmission with conferenceId and paperId')
    return null
  }
}
