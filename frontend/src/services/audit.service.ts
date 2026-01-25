import apiClient from './api'

/**
 * Audit Log DTO interface
 */
export interface AuditLogDTO {
  id: number
  userId: number
  username: string
  action: string
  resource: string
  resourceId?: number
  details?: string
  ipAddress?: string
  userAgent?: string
  timestamp: string
}

/**
 * Page response interface
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

/**
 * Audit log query parameters
 */
export interface AuditLogQueryParams {
  userId?: number
  action?: string
  resource?: string
  resourceId?: number
  startDate?: string
  endDate?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'ASC' | 'DESC'
}

/**
 * Audit Service - Xử lý các API calls liên quan đến audit logs
 */
export const auditService = {
  /**
   * Lấy danh sách audit logs với filters và pagination (ADMIN only)
   * GET /api/audit-logs
   */
  getAuditLogs: async (params: AuditLogQueryParams = {}): Promise<PageResponse<AuditLogDTO>> => {
    const response = await apiClient.get<PageResponse<AuditLogDTO>>('/audit-logs', {
      params,
    })
    return response.data
  },

  /**
   * Lấy thông tin audit log theo ID (ADMIN only)
   * GET /api/audit-logs/{id}
   */
  getAuditLogById: async (id: number): Promise<AuditLogDTO> => {
    const response = await apiClient.get<AuditLogDTO>(`/audit-logs/${id}`)
    return response.data
  },

  /**
   * Export audit logs (ADMIN only)
   * GET /api/audit-logs/export
   */
  exportAuditLogs: async (
    params: AuditLogQueryParams = {},
    format: 'CSV' = 'CSV',
  ): Promise<Blob> => {
    const response = await apiClient.get('/audit-logs/export', {
      params: { ...params, format },
      responseType: 'blob',
    })
    return response.data
  },
}
