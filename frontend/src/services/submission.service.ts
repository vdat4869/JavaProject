import apiClient from './api'

/**
 * Submission Author interface
 */
export interface SubmissionAuthor {
  id?: number
  userId?: number
  firstName: string
  lastName: string
  email?: string
  affiliation?: string
  isCorresponding?: boolean
  orderIndex?: number
}

/**
 * Submission interface - matches backend SubmissionResponseDTO
 */
export interface Submission {
  id: number
  conferenceId: number
  authorId: number
  title: string
  abstractText: string // Note: backend uses "abstractText" not "abstract"
  status:
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'REVIEWED'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'WITHDRAWN'
  | 'CAMERA_READY'
  pdfFilePath?: string
  trackId?: number
  keywords?: string // Note: backend stores as string, not array
  withdrawn?: boolean
  authors?: SubmissionAuthor[]
  files?: SubmissionFile[]
  createdAt?: string
  updatedAt?: string
  // Frontend convenience fields (computed from above)
  conferenceName?: string
  trackName?: string
  submittedAt?: string
  deadline?: string
  canEdit?: boolean
  canWithdraw?: boolean
  fileUrl?: string
  fileName?: string
  // Legacy field for backward compatibility
  abstract?: string
  keywordsArray?: string[]
}

/**
 * Create Submission request - matches backend SubmissionCreateDTO
 */
export interface CreateSubmissionRequest {
  conferenceId: number
  title: string
  abstractText: string // Note: backend uses "abstractText"
  trackId?: number
  keywords?: string | string[] // Note: backend stores as string, not array
  authors?: SubmissionAuthor[]
}

/**
 * Update Submission request - matches backend SubmissionUpdateDTO
 */
export interface UpdateSubmissionRequest {
  title?: string
  abstractText?: string // Note: backend uses "abstractText"
  trackId?: number
  keywords?: string | string[] // Note: backend stores as string, not array
  authors?: SubmissionAuthor[]
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
 * Submission File interface - matches backend SubmissionFileDTO
 */
export interface SubmissionFile {
  id: number
  versionNumber: number
  fileName: string
  filePath: string
  fileSize: number
  contentType: string
  isCurrent: boolean
  uploadedAt: string
  uploadNote?: string
}

/**
 * Submission Service - Xử lý các API calls liên quan đến submissions
 */
export const submissionService = {
  /**
   * Lấy danh sách submissions của user hiện tại
   * GET /api/submissions/my
   */
  getMySubmissions: async (): Promise<Submission[]> => {
    const response = await apiClient.get<{ success: boolean; data: Submission[] }>('/submissions/my')
    const submissions = response.data.data || response.data
    // Transform for frontend compatibility
    return (Array.isArray(submissions) ? submissions : []).map((s) => ({
      ...s,
      abstract: s.abstractText, // Legacy field
      keywordsArray: s.keywords ? s.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }))
  },

  /**
   * Lấy thông tin submission theo ID
   * GET /api/submissions/{id}
   */
  getSubmission: async (id: number): Promise<Submission> => {
    const response = await apiClient.get<{ success: boolean; data: Submission }>(`/submissions/${id}`)
    const submission = response.data.data || response.data
    // Transform for frontend compatibility
    return {
      ...submission,
      abstract: submission.abstractText, // Legacy field
      keywordsArray: submission.keywords ? submission.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }
  },

  /**
   * Alias for getSubmission
   */
  getSubmissionById: async (id: number): Promise<Submission> => {
    return submissionService.getSubmission(id)
  },

  /**
   * Lấy danh sách submissions của conference (CHAIR/ADMIN only)
   * GET /api/submissions/conference/{conferenceId}
   */
  getSubmissionsByConference: async (conferenceId: number): Promise<Submission[]> => {
    const response = await apiClient.get<{ success: boolean; data: Submission[] }>(
      `/submissions/conference/${conferenceId}`
    )
    const submissions = response.data.data || response.data
    // Transform for frontend compatibility
    return (Array.isArray(submissions) ? submissions : []).map((s) => ({
      ...s,
      abstract: s.abstractText, // Legacy field
      keywordsArray: s.keywords ? s.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }))
  },

  /**
   * Tạo submission mới
   * POST /api/submissions
   * Note: File upload is separate - use uploadPdf after creating submission
   */
  createSubmission: async (data: CreateSubmissionRequest): Promise<Submission> => {
    // Convert keywords array to string if provided
    const requestData: any = {
      conferenceId: data.conferenceId,
      title: data.title,
      abstractText: data.abstractText,
    }
    if (data.trackId) {
      requestData.trackId = data.trackId
    }
    if (data.keywords) {
      requestData.keywords = typeof data.keywords === 'string' ? data.keywords : data.keywords.join(', ')
    }
    if (data.authors && data.authors.length > 0) {
      requestData.authors = data.authors
    }

    const response = await apiClient.post<{ success: boolean; data: Submission }>('/submissions', requestData)
    const submission = response.data.data || response.data
    // Transform for frontend compatibility
    return {
      ...submission,
      abstract: submission.abstractText, // Legacy field
      keywordsArray: submission.keywords ? submission.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }
  },

  /**
   * Cập nhật submission
   * PUT /api/submissions/{id}
   * Note: File upload is separate - use uploadPdf for file updates
   */
  updateSubmission: async (id: number, data: UpdateSubmissionRequest): Promise<Submission> => {
    const requestData: any = {}
    if (data.title) {
      requestData.title = data.title
    }
    if (data.abstractText) {
      requestData.abstractText = data.abstractText
    }
    if (data.trackId !== undefined) {
      requestData.trackId = data.trackId
    }
    if (data.keywords !== undefined) {
      requestData.keywords = typeof data.keywords === 'string' ? data.keywords : data.keywords.join(', ')
    }
    if (data.authors) {
      requestData.authors = data.authors
    }

    const response = await apiClient.put<{ success: boolean; data: Submission }>(`/submissions/${id}`, requestData)
    const submission = response.data.data || response.data
    // Transform for frontend compatibility
    return {
      ...submission,
      abstract: submission.abstractText, // Legacy field
      keywordsArray: submission.keywords ? submission.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }
  },

  /**
   * Xóa submission draft (chỉ cho phép xóa draft chưa submit)
   * DELETE /api/submissions/{id}
   */
  deleteSubmission: async (id: number): Promise<void> => {
    await apiClient.delete(`/submissions/${id}`)
  },

  /**
   * Upload PDF file cho submission
   * POST /api/submissions/{id}/upload-pdf
   */
  uploadPdf: async (id: number, file: File): Promise<SubmissionFile> => {
    const formData = new FormData()
    formData.append('file', file)

    const response = await apiClient.post<{ success: boolean; data: SubmissionFile }>(
      `/submissions/${id}/upload-pdf`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
    return response.data.data || response.data
  },

  /**
   * Withdraw submission (rút bài đã submit)
   * POST /api/submissions/{id}/withdraw
   */
  withdrawSubmission: async (id: number): Promise<Submission> => {
    const response = await apiClient.post<{ success: boolean; data: Submission }>(`/submissions/${id}/withdraw`)
    const submission = response.data.data || response.data
    // Transform for frontend compatibility
    return {
      ...submission,
      abstract: submission.abstractText, // Legacy field
      keywordsArray: submission.keywords ? submission.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }
  },

  /**
   * Submit submission (nộp bài)
   * POST /api/submissions/{id}/submit
   */
  submitSubmission: async (id: number): Promise<Submission> => {
    const response = await apiClient.post<{ success: boolean; data: Submission }>(`/submissions/${id}/submit`)
    const submission = response.data.data || response.data
    // Transform for frontend compatibility
    return {
      ...submission,
      abstract: submission.abstractText, // Legacy field
      keywordsArray: submission.keywords ? submission.keywords.split(',').map((k: string) => k.trim()) : [], // Legacy field
    }
  },

  /**
   * Lấy danh sách reviews cho submission (anonymized)
   * GET /api/reviews/submission/{id}
   */
  getReviews: async (id: number): Promise<Review[]> => {
    const response = await apiClient.get<any>(`/reviews/submission/${id}`)
    return response.data.data || []
  },

  /**
   * Lấy decision cho submission
   * GET /api/decisions/submission/{id}
   */
  getDecision: async (id: number): Promise<Decision | null> => {
    try {
      const response = await apiClient.get<any>(`/decisions/submission/${id}`)
      return response.data.data
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null // Chưa có decision
      }
      throw error
    }
  },

  /**
   * Download submission file hiện tại
   * GET /api/submissions/{id}/file
   */
  downloadFile: async (id: number): Promise<Blob> => {
    const response = await apiClient.get(`/submissions/${id}/file`, {
      responseType: 'blob',
    })
    return response.data
  },

  /**
   * Lấy danh sách tất cả các version của PDF file đã upload
   * GET /api/submissions/{id}/files
   */
  getFileVersions: async (id: number): Promise<SubmissionFile[]> => {
    const response = await apiClient.get<{ success: boolean; data: SubmissionFile[] }>(`/submissions/${id}/files`)
    return response.data.data || response.data
  },

  /**
   * Download một version cụ thể của PDF file
   * GET /api/submissions/{id}/files/{fileId}
   */
  downloadFileVersion: async (id: number, fileId: number): Promise<Blob> => {
    const response = await apiClient.get(`/submissions/${id}/files/${fileId}`, {
      responseType: 'blob',
    })
    return response.data
  },
}
