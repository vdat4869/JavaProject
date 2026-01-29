import apiClient from './api'

/**
 * User DTO interface
 */
export interface UserDTO {
  id: number
  email: string
  firstName: string
  lastName: string
  organizationId?: number
  organizationName?: string
  phone?: string
  emailVerified: boolean
  active: boolean
  roles: string[]
  createdAt: string
  updatedAt: string
}

/**
 * User stats interface
 */
export interface UserStats {
  activeUsers: number
  verifiedUsers: number
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
 * User Service - Xử lý các API calls liên quan đến user management
 */
export const userService = {
  /**
   * Lấy thông tin user hiện tại
   * GET /api/users/me
   */
  getCurrentUser: async (): Promise<UserDTO> => {
    const response = await apiClient.get<any>('/users/me')
    return response.data?.data || response.data
  },

  /**
   * Cập nhật thông tin profile của user hiện tại
   * PUT /api/users/me
   */
  updateCurrentUser: async (data: Partial<UserDTO>): Promise<UserDTO> => {
    const response = await apiClient.put<any>('/users/me', data)
    return response.data?.data || response.data
  },

  /**
   * Lấy thông tin user theo ID (ADMIN only)
   * GET /api/users/{id}
   */
  getUserById: async (id: number): Promise<UserDTO> => {
    const response = await apiClient.get<any>(`/users/${id}`)
    return response.data?.data || response.data
  },

  /**
   * Lấy danh sách tất cả user (ADMIN only)
   * GET /api/users?page={page}&size={size}
   */
  getAllUsers: async (page: number = 0, size: number = 20): Promise<PageResponse<UserDTO>> => {
    const response = await apiClient.get<any>('/users', {
      params: { page, size },
    })
    return response.data?.data || response.data
  },

  /**
   * Lấy danh sách user đang active (ADMIN only)
   * GET /api/users/active/list?page={page}&size={size}
   */
  getActiveUsers: async (page: number = 0, size: number = 20): Promise<PageResponse<UserDTO>> => {
    const response = await apiClient.get<any>('/users/active/list', {
      params: { page, size },
    })
    return response.data?.data || response.data
  },

  /**
   * Tìm kiếm user theo tên hoặc email (ADMIN only)
   * GET /api/users/search?keyword={keyword}&page={page}&size={size}
   */
  searchUsers: async (
    keyword: string,
    page: number = 0,
    size: number = 20,
  ): Promise<PageResponse<UserDTO>> => {
    const response = await apiClient.get<any>('/users/search', {
      params: { keyword, page, size },
    })
    return response.data?.data || response.data
  },

  /**
   * Deactivate user account (ADMIN only)
   * PUT /api/users/{id}/deactivate
   */
  deactivateUser: async (id: number): Promise<void> => {
    await apiClient.put(`/users/${id}/deactivate`)
  },

  /**
   * Activate user account (ADMIN only)
   * PUT /api/users/{id}/activate
   */
  activateUser: async (id: number): Promise<void> => {
    await apiClient.put(`/users/${id}/activate`)
  },

  /**
   * Lấy thống kê về user (ADMIN only)
   * GET /api/users/stats/summary
   */
  getUserStats: async (): Promise<UserStats> => {
    const response = await apiClient.get<any>('/users/stats/summary')
    return response.data?.data || response.data
  },

  /**
   * Cập nhật quyền (roles) cho user (ADMIN only)
   * PUT /api/users/{id}/roles
   */
  updateUserRoles: async (id: number, roles: string[]): Promise<void> => {
    await apiClient.put(`/users/${id}/roles`, roles)
  },
}
