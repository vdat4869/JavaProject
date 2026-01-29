import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react'

/**
 * User roles trong hệ thống
 */
export type UserRole = 'GUEST' | 'AUTHOR' | 'PC' | 'CHAIR' | 'ADMIN'

/**
 * User interface
 */
export interface User {
  id: number
  email: string
  fullName: string
  roles: UserRole[]
  emailVerified: boolean
}

/**
 * Auth context state
 */
interface AuthContextType {
  user: User | null
  loading: boolean
  isAuthenticated: boolean
  login: (
    email: string,
    password: string,
  ) => Promise<{ success: boolean; error?: string; requiresVerification?: boolean }>
  handleSSOCallback: (
    code: string,
    state?: string | null,
  ) => Promise<{ success: boolean; error?: string }>
  logout: () => Promise<void>
  hasRole: (role: UserRole) => boolean
  hasAnyRole: (roles: UserRole[]) => boolean
  refreshUser: () => Promise<void>
  loginWithTokens: (accessToken: string, refreshToken: string) => Promise<{ success: boolean; error?: string }>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

/**
 * AuthProvider Props
 */
interface AuthProviderProps {
  children: ReactNode
}

/**
 * Normalize roles from backend to frontend UserRole type
 * - Convert to uppercase
 * - Filter invalid roles
 */
const normalizeRoles = (roles: any[] | undefined): UserRole[] => {
  if (!roles || roles.length === 0) {
    return []
  }
  return roles
    .map((role: any) => {
      const roleStr = typeof role === 'string' ? role : role.name || ''
      return roleStr.toUpperCase().replace('ROLE_', '') as UserRole
    })
    .filter((role: UserRole) =>
      ['GUEST', 'AUTHOR', 'PC', 'CHAIR', 'ADMIN'].includes(role)
    )
}

/**
 * AuthProvider - Quản lý authentication state cho toàn bộ ứng dụng
 *
 * Features:
 * - JWT token management
 * - User role checking
 * - Auto-check authentication on mount
 * - Login/logout functions
 */
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false)

  /**
   * Kiểm tra authentication status khi component mount
   */
  useEffect(() => {
    checkAuth()
  }, [])

  /**
   * Kiểm tra authentication từ localStorage và API
   * Tối ưu: Chỉ check localStorage ngay, verify token async sau
   */
  const checkAuth = async (): Promise<void> => {
    try {
      const token = localStorage.getItem('accessToken')
      const storedUser = localStorage.getItem('user')

      if (token && storedUser) {
        // Parse user từ localStorage ngay để UI hiển thị nhanh
        const userData: User = JSON.parse(storedUser)
        setUser(userData)
        setIsAuthenticated(true)
        setLoading(false) // Set loading false ngay để UI render

        // Verify token với backend async (không block UI)
        // Có thể verify sau hoặc skip để tăng tốc
        // await verifyToken()
      } else {
        setLoading(false)
      }
    } catch (error) {
      console.error('Error checking auth:', error)
      // Clear invalid data
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      setUser(null)
      setIsAuthenticated(false)
      setLoading(false)
    }
  }

  /**
   * Đăng nhập với email và password (Local account)
   */
  const login = useCallback(async (
    email: string,
    password: string,
  ): Promise<{ success: boolean; error?: string; requiresVerification?: boolean }> => {
    try {
      const { authService } = await import('../services/auth.service')
      const response = await authService.login({ email, password })

      const { accessToken, refreshToken, userId, ...userData } = response

      // Lưu tokens và user info
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)

      // Normalize roles từ backend
      const normalizedRoles = normalizeRoles(userData.roles)

      // Map userId to id for User interface
      const user: User = {
        id: userId,
        email: userData.email,
        fullName: userData.fullName,
        roles: normalizedRoles,
        emailVerified: userData.emailVerified,
      }
      localStorage.setItem('user', JSON.stringify(user))

      setUser(user)
      setIsAuthenticated(true)

      return { success: true }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại.'
      return {
        success: false,
        error: errorMessage,
      }
    }
  }, [])

  /**
   * Đăng nhập trực tiếp với tokens (thường dùng sau SSO)
   */
  const loginWithTokens = useCallback(async (
    accessToken: string,
    refreshToken: string,
  ): Promise<{ success: boolean; error?: string }> => {
    try {
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)

      // Set authenticated trước để AppLayout không redirect về login
      setIsAuthenticated(true)

      // Sau đó mới lấy thông tin user chi tiết
      await refreshUser()

      return { success: true }
    } catch (error: any) {
      console.error('Login with tokens failed:', error)
      return {
        success: false,
        error: 'Failed to establish session with tokens.',
      }
    }
  }, [])

  /**
   * Xử lý SSO callback và đăng nhập
   */
  const handleSSOCallback = useCallback(async (
    code: string,
    state?: string | null,
  ): Promise<{ success: boolean; error?: string }> => {
    try {
      const { authService } = await import('../services/auth.service')
      const response = await authService.handleSSOCallback({ code, state })

      const { accessToken, refreshToken, userId, ...userData } = response

      // Lưu tokens và user info
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)

      // Normalize roles từ backend
      const normalizedRoles = normalizeRoles(userData.roles)

      // Map userId to id for User interface
      const user: User = {
        id: userId,
        email: userData.email,
        fullName: userData.fullName,
        roles: normalizedRoles,
        emailVerified: userData.emailVerified,
      }
      localStorage.setItem('user', JSON.stringify(user))

      setUser(user)
      setIsAuthenticated(true)

      return { success: true }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'SSO authentication failed.'
      return {
        success: false,
        error: errorMessage,
      }
    }
  }, [])

  /**
   * Đăng xuất
   */
  const logout = useCallback(async (): Promise<void> => {
    try {
      const { authService } = await import('../services/auth.service')
      await authService.logout()
    } catch (error) {
      console.error('Logout error:', error)
      // Continue với logout dù API call fail
    } finally {
      // Clear local storage
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')

      setUser(null)
      setIsAuthenticated(false)
    }
  }, [])

  /**
   * Kiểm tra user có role cụ thể không
   */
  const hasRole = (role: UserRole): boolean => {
    if (!user || !user.roles) return false
    return user.roles.includes(role)
  }

  /**
   * Kiểm tra user có bất kỳ role nào trong danh sách
   */
  const hasAnyRole = (roles: UserRole[]): boolean => {
    if (!user || !user.roles) return false
    return roles.some((role) => user.roles.includes(role))
  }

  /**
   * Refresh user data từ API
   */
  const refreshUser = useCallback(async (): Promise<void> => {
    try {
      const { authService } = await import('../services/auth.service')
      const response: any = await authService.getCurrentUser()

      // Backend wraps in ApiResponse: { success: true, data: UserDTO }
      const userData = response.data || response

      // Normalize roles từ backend
      const normalizedRoles = normalizeRoles(userData.roles)

      // Map userId to id for User interface
      const user: User = {
        id: userData.id || userData.userId,
        email: userData.email,
        fullName: userData.fullName || `${userData.firstName} ${userData.lastName}`,
        roles: normalizedRoles,
        emailVerified: userData.emailVerified,
      }
      setUser(user)
      localStorage.setItem('user', JSON.stringify(user))
    } catch (error) {
      console.error('Error refreshing user:', error)
      throw error // Rethrow to let caller know it failed
    }
  }, [])

  const value: AuthContextType = {
    user,
    loading,
    isAuthenticated,
    login,
    loginWithTokens,
    handleSSOCallback,
    logout,
    hasRole,
    hasAnyRole,
    refreshUser,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * Hook để sử dụng AuthContext
 */
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
