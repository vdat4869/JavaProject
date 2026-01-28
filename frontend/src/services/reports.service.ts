import apiClient from './api'

/**
 * Report Statistics interface (matches ReportResponseDTO)
 */
export interface ReportStatistics {
  id?: number
  conferenceId: number
  totalSubmissions: number
  acceptedCount: number
  rejectedCount: number
  pendingCount: number
  acceptanceRate: number
  totalReviews: number
  completedReviews: number
  pendingReviews: number
  totalAssignments: number
  acceptedAssignments: number
  declinedAssignments: number
  snapshotAt?: string
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
 * Reports Service - API v1 (/api/reporting)
 */
export const reportsService = {
  /**
   * Lấy báo cáo mới nhất (hoặc generate on-the-fly)
   * GET /api/reporting/conference/{id}
   */
  getLatestReport: async (conferenceId: number): Promise<ReportStatistics> => {
    const response = await apiClient.get<any>(
      `/reporting/conference/${conferenceId}`
    )
    return response.data?.data || response.data
  },

  /**
   * Tạo snapshot mới
   * POST /api/reporting/conference/{id}/snapshot
   */
  createSnapshot: async (conferenceId: number): Promise<ReportStatistics> => {
    const response = await apiClient.post<any>(
      `/reporting/conference/${conferenceId}/snapshot`
    )
    return response.data?.data || response.data
  },

  /**
   * Lấy lịch sử snapshots
   * GET /api/reporting/conference/{id}/history
   */
  getReportHistory: async (conferenceId: number): Promise<ReportStatistics[]> => {
    const response = await apiClient.get<any>(
      `/reporting/conference/${conferenceId}/history`
    )
    const data = response.data?.data || response.data
    return Array.isArray(data) ? data : []
  },

  /**
   * Legacy wrapper for backward compatibility
   */
  getStatistics: async (conferenceId: number): Promise<any> => {
    return reportsService.getLatestReport(conferenceId)
  },

  /**
   * Export report (Giữ nguyên path cũ nếu backend chưa refactor endpoint này)
   */
  export: async (data: ReportExportRequest): Promise<{ fileName: string }> => {
    const params = new URLSearchParams({
      conferenceId: data.conferenceId.toString(),
      reportType: data.reportType,
      format: data.format,
    })

    const response = await apiClient.get(
      `/reports/export?${params.toString()}`,
      { responseType: 'blob' }
    )

    // Extract filename from Content-Disposition header
    const contentDisposition = response.headers['content-disposition']
    let fileName = `report_${data.conferenceId}_${new Date().toISOString().split('T')[0]}.${data.format.toLowerCase()}`
    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (match && match[1]) {
        fileName = match[1].replace(/['"]/g, '')
      }
    }

    // Trigger download
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)

    return { fileName }
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
