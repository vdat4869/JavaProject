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
  fullName: string
}

/**
 * Verify Email request
 */
export interface VerifyEmailRequest {
  token: string
}

/**
 * Auth Service - Xử lý tất cả các API calls liên quan đến authentication
 */
export const authService = {
  /**
   * Đăng ký tài khoản mới
   * POST /api/auth/register
   */
  register: async (data: RegisterRequest): Promise<void> => {
    await apiClient.post('/auth/register', data)
  },

  /**
   * Đăng nhập với email và password (Local account)
   * POST /api/auth/login
   */
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>('/auth/login', credentials)
    return response.data
  },

  /**
   * Lấy SSO redirect URL
   * GET /api/auth/sso/redirect
   */
  getSSORedirectUrl: async (): Promise<string> => {
    const response = await apiClient.get<SSORedirectResponse>('/auth/sso/redirect')
    return response.data.redirectUrl
  },

  /**
   * Xử lý SSO callback sau khi user authenticate với SSO provider
   * POST /api/auth/sso/callback
   */
  handleSSOCallback: async (data: SSOCallbackRequest): Promise<SSOCallbackResponse> => {
    const response = await apiClient.post<SSOCallbackResponse>('/auth/sso/callback', data)
    return response.data
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
    const response = await apiClient.get<LoginResponse>('/auth/me')
    return response.data
  },

  /**
   * Đăng xuất
   * POST /api/auth/logout
   */
  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout')
  },
}
