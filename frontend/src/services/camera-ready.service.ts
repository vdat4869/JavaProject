import apiClient from './api'

/**
 * Camera-ready Submission interface
 */
export interface CameraReadySubmission {
  id: number
  submissionId: number
  submissionTitle: string
  filePath: string
  fileName: string
  formatChecked: boolean
  formatIssues?: string[]
  submittedAt: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'NEEDS_REVISION'
}

/**
 * Upload Camera-ready request
 */
export interface UploadCameraReadyRequest {
  submissionId: number
  file: File
}

/**
 * Format Check result
 */
export interface FormatCheckResult {
  passed: boolean
  issues: Array<{
    type: 'ERROR' | 'WARNING' | 'INFO'
    message: string
    page?: number
  }>
}

/**
 * Camera-ready Service - Xử lý các API calls liên quan đến camera-ready submissions
 */
export const cameraReadyService = {
  /**
   * Lấy camera-ready submission theo submission ID
   * GET /api/camera-ready/submission/{submissionId}
   */
  getBySubmissionId: async (submissionId: number): Promise<CameraReadySubmission | null> => {
    try {
      const response = await apiClient.get<CameraReadySubmission>(
        `/camera-ready/submission/${submissionId}`,
      )
      return response.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  },

  /**
   * Upload camera-ready file
   * POST /api/camera-ready
   */
  upload: async (data: UploadCameraReadyRequest): Promise<CameraReadySubmission> => {
    const formData = new FormData()
    formData.append('submissionId', data.submissionId.toString())
    formData.append('file', data.file)

    const response = await apiClient.post<CameraReadySubmission>('/camera-ready', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Cập nhật camera-ready submission
   * PUT /api/camera-ready/{id}
   */
  update: async (id: number, file: File): Promise<CameraReadySubmission> => {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.put<CameraReadySubmission>(`/camera-ready/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Kiểm tra format của camera-ready file
   * POST /api/camera-ready/{id}/format-check
   */
  checkFormat: async (id: number): Promise<FormatCheckResult> => {
    const response = await apiClient.post<FormatCheckResult>(`/camera-ready/${id}/format-check`)
    return response.data
  },

  /**
   * Lấy danh sách camera-ready submissions (CHAIR only)
   * GET /api/camera-ready?conferenceId={id}
   */
  getAll: async (conferenceId: number): Promise<CameraReadySubmission[]> => {
    const response = await apiClient.get<CameraReadySubmission[]>(
      `/camera-ready?conferenceId=${conferenceId}`,
    )
    return response.data
  },
}
