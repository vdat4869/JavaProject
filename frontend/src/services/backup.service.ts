import apiClient from './api'

export interface BackupRecord {
    id: number
    conferenceId?: number
    backupType: 'DATABASE' | 'STORAGE' | 'FULL'
    filePath: string
    fileSize: number
    status: 'PENDING' | 'COMPLETED' | 'FAILED'
    createdAt: string
    createdBy: number
}

/**
 * Backup Service - Admin tool for data protection (API v1)
 */
export const backupService = {
    /**
     * Lấy danh sách tất cả các bản sao lưu
     * GET /api/storage/backup
     */
    getAllBackups: async (): Promise<BackupRecord[]> => {
        const response = await apiClient.get<any>('/storage/backup')
        return response.data?.data || response.data || []
    },

    /**
     * Lấy lịch sử sao lưu của một hội nghị cụ thể
     * GET /api/storage/backup/conference/{id}
     */
    getConferenceBackups: async (conferenceId: number): Promise<BackupRecord[]> => {
        const response = await apiClient.get<any>(`/storage/backup/conference/${conferenceId}`)
        return response.data?.data || response.data || []
    },

    /**
     * Tạo bản sao lưu toàn bộ hệ thống
     * POST /api/storage/backup/all
     */
    backupAll: async (): Promise<BackupRecord> => {
        const response = await apiClient.post<any>('/storage/backup/all')
        return response.data?.data || response.data
    },

    /**
     * Tạo bản sao lưu cho một hội nghị cụ thể
     * POST /api/storage/backup/conference/{id}
     */
    backupConference: async (conferenceId: number): Promise<BackupRecord> => {
        const response = await apiClient.post<any>(`/storage/backup/conference/${conferenceId}`)
        return response.data?.data || response.data
    },

    /**
     * Khôi phục từ một bản sao lưu cụ thể
     * POST /api/storage/backup/{id}/restore
     */
    restore: async (backupId: number): Promise<boolean> => {
        const response = await apiClient.post<any>(`/storage/backup/${backupId}/restore`)
        return response.data?.data ?? response.data ?? false
    },

    /**
     * Khôi phục hội nghị từ bản sao lưu mới nhất
     * POST /api/storage/backup/conference/{id}/restore
     */
    restoreConference: async (conferenceId: number): Promise<boolean> => {
        const response = await apiClient.post<any>(`/storage/backup/conference/${conferenceId}/restore`)
        return response.data?.data ?? response.data ?? false
    },
}
