import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'

/**
 * Axios instance với interceptors cho UTH-ConfMS
 *
 * Features:
 * - Tự động thêm JWT token vào request headers
 * - Tự động refresh token khi access token hết hạn
 * - Xử lý lỗi authentication và redirect về login
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
})

/**
 * Request interceptor: Thêm JWT token vào header
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Add X-User-Id header if user is logged in
    const userStr = localStorage.getItem('user')
    if (userStr && config.headers) {
      try {
        const user = JSON.parse(userStr)
        if (user && user.id) {
          config.headers['X-User-Id'] = user.id.toString()
        }
      } catch (e) {
        // Ignore JSON parse error
      }
    }

    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  },
)

/**
 * Response interceptor: Xử lý refresh token và errors
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // Nếu lỗi 401 (Unauthorized) và chưa retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
          // Gọi API refresh token với header Authorization
          // Sử dụng axios trực tiếp (không qua apiClient) để tránh interceptor loop
          const response = await axios.post(
            '/api/auth/refresh',
            {}, // Empty body
            {
              headers: {
                Authorization: `Bearer ${refreshToken}`,
              },
            }
          )

          // Backend trả về: { success: true, data: { accessToken, tokenType } }
          const responseData = response.data?.data || response.data
          const newAccessToken = responseData?.accessToken

          if (!newAccessToken) {
            throw new Error('No access token in refresh response')
          }

          localStorage.setItem('accessToken', newAccessToken)

          // Retry original request với token mới
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          }
          return apiClient(originalRequest)
        }
      } catch (refreshError) {
        // Refresh token failed, logout user
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')

        // Redirect to login
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  },
)

export default apiClient
