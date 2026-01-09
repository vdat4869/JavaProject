import apiClient from './api'

/**
 * Report Statistics interface
 */
export interface ReportStatistics {
  totalSubmissions: number
  acceptedSubmissions: number
  rejectedSubmissions: number
  pendingSubmissions: number
  totalReviews: number
  completedReviews: number
  averageRating: number
  submissionsByTrack: Array<{
    trackName: string
    count: number
    accepted: number
    rejected: number
  }>
  submissionsByStatus: Array<{
    status: string
    count: number
  }>
  reviewProgress: {
    completed: number
    inProgress: number
    pending: number
  }
}

/**
 * Report Export request
 */
export interface ReportExportRequest {
  conferenceId: number
  reportType: 'STATISTICS' | 'SUBMISSIONS' | 'REVIEWS' | 'DECISIONS' | 'ALL'
  format: 'PDF' | 'EXCEL' | 'CSV'
}

/**
 * Report Export response
 */
export interface ReportExportResponse {
  downloadUrl: string
  fileName: string
  fileSize: number
  generatedAt: string
}

/**
 * Reports Service - Xử lý các API calls liên quan đến reports
 */
export const reportsService = {
  /**
   * Lấy thống kê cho conference
   * GET /api/reports/statistics?conferenceId={id}
   */
  getStatistics: async (conferenceId: number): Promise<ReportStatistics> => {
    const response = await apiClient.get<ReportStatistics>(
      `/reports/statistics?conferenceId=${conferenceId}`,
    )
    return response.data
  },

  /**
   * Export report
   * GET /api/reports/export?conferenceId={id}&reportType={type}&format={format}
   */
  export: async (data: ReportExportRequest): Promise<ReportExportResponse> => {
    const params = new URLSearchParams({
      conferenceId: data.conferenceId.toString(),
      reportType: data.reportType,
      format: data.format,
    })

    const response = await apiClient.get<ReportExportResponse>(
      `/reports/export?${params.toString()}`,
    )
    return response.data
  },

  /**
   * Download report file
   */
  download: async (downloadUrl: string, fileName: string): Promise<void> => {
    const response = await apiClient.get(downloadUrl, {
      responseType: 'blob',
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  },
}
