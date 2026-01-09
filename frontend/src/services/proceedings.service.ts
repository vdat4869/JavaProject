import apiClient from './api'

/**
 * Proceedings Export request
 */
export interface ProceedingsExportRequest {
  conferenceId: number
  format: 'PDF' | 'ZIP' | 'BOTH'
  includeAcceptedOnly?: boolean
  includeAbstracts?: boolean
}

/**
 * Proceedings Export response
 */
export interface ProceedingsExportResponse {
  downloadUrl: string
  fileName: string
  fileSize: number
  exportedAt: string
}

/**
 * Proceedings Service - Xử lý các API calls liên quan đến proceedings
 */
export const proceedingsService = {
  /**
   * Export proceedings
   * GET /api/proceedings/export?conferenceId={id}&format={format}
   */
  export: async (data: ProceedingsExportRequest): Promise<ProceedingsExportResponse> => {
    const params = new URLSearchParams({
      conferenceId: data.conferenceId.toString(),
      format: data.format,
    })
    if (data.includeAcceptedOnly !== undefined) {
      params.append('includeAcceptedOnly', data.includeAcceptedOnly.toString())
    }
    if (data.includeAbstracts !== undefined) {
      params.append('includeAbstracts', data.includeAbstracts.toString())
    }

    const response = await apiClient.get<ProceedingsExportResponse>(
      `/proceedings/export?${params.toString()}`,
    )
    return response.data
  },

  /**
   * Download proceedings file
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
