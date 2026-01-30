import apiClient from './api'

/**
 * Login request payload
 */
export interface LoginRequest {
  email: string
  password: string
}

/**
 * Login response
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  userId: number
  email: string
  fullName: string
  roles: string[]
  emailVerified: boolean
}

/**
 * SSO Redirect response
 */
export interface SSORedirectResponse {
  redirectUrl: string
}

/**
 * SSO Callback request
 */
export interface SSOCallbackRequest {
  code: string
  state?: string | null
}

/**
 * SSO Callback response (same as LoginResponse)
 */
export type SSOCallbackResponse = LoginResponse

/**
 * Register request payload
 */
export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  organizationId: number | null
}

/**
 * Verify Email request
 */
export interface VerifyEmailRequest {
  token: string
}

/**
 * Forgot Password request
 */
export interface ForgotPasswordRequest {
  email: string
}

/**
 * Reset Password request
 */
export interface ResetPasswordRequest {
  token: string
  newPassword: String
}

/**
 * Auth Service - Xử lý tất cả các API calls liên quan đến authentication
 */
export const authService = {
  /**
   * Đăng ký tài khoản mới
   * POST /api/auth/register
   */
  register: async (data: RegisterRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<any>('/auth/register', data)
    return response.data?.data || response.data
  },

  /**
   * Đăng nhập với email và password (Local account)
   * POST /api/auth/login
   */
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<any>('/auth/login', credentials)
    return response.data?.data || response.data
  },

  /**
   * Lấy SSO redirect URL
   * GET /api/auth/sso/redirect
   */
  getSSORedirectUrl: async (): Promise<string> => {
    const response = await apiClient.get<{ success: boolean; data: SSORedirectResponse }>('/auth/sso/redirect')
    // Backend wraps response in ApiResponse: { success: true, data: { redirectUrl: "..." } }
    const respData = response.data as any
    return respData.data?.redirectUrl || respData.redirectUrl
  },

  /**
   * Xử lý SSO callback sau khi user authenticate với SSO provider
   * POST /api/auth/sso/callback
   */
  handleSSOCallback: async (data: SSOCallbackRequest): Promise<SSOCallbackResponse> => {
    const response = await apiClient.post<any>('/auth/sso/callback', data)
    return response.data?.data || response.data
  },

  /**
   * Xác thực email với token
   * POST /api/auth/verify-email?token={token}
   */
  verifyEmail: async (token: string): Promise<void> => {
    await apiClient.post(`/auth/verify-email?token=${token}`)
  },

  /**
   * Lấy thông tin user hiện tại
   * GET /api/auth/me
   */
  getCurrentUser: async (): Promise<LoginResponse> => {
    const response = await apiClient.get<any>('/users/me')
    return response.data?.data || response.data
  },

  /**
   * Đăng xuất
   * POST /api/auth/logout
   */
  logout: async (): Promise<void> => {
    const refreshToken = localStorage.getItem('refreshToken')
    await apiClient.post('/auth/logout', { refreshToken })
  },

  /**
   * Change password
   * POST /api/auth/change-password
   */
  changePassword: async (data: { currentPassword: string; newPassword: string }): Promise<void> => {
    await apiClient.post('/auth/change-password', data)
  },

  /**
   * Request password reset link
   * POST /api/auth/forgot-password
   */
  forgotPassword: async (data: ForgotPasswordRequest): Promise<void> => {
    await apiClient.post('/auth/forgot-password', data)
  },

  /**
   * Reset password using token
   * POST /api/auth/reset-password
   */
  resetPassword: async (data: ResetPasswordRequest): Promise<void> => {
    await apiClient.post('/auth/reset-password', data)
  },
}
