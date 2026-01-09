import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'

/**
 * User roles trong hệ thống
 */
export type UserRole = 'GUEST' | 'AUTHOR' | 'REVIEWER' | 'PC' | 'PC_MEMBER' | 'CHAIR' | 'ADMIN'

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
const normalizeRoles = (roles: string[] | undefined): UserRole[] => {
  if (!roles || roles.length === 0) {
    return []
  }
  return roles
    .map((role: string) => role.toUpperCase() as UserRole)
    .filter((role: UserRole) => 
      ['GUEST', 'AUTHOR', 'REVIEWER', 'PC', 'PC_MEMBER', 'CHAIR', 'ADMIN'].includes(role)
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
  const login = async (
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

      // TODO: Email verification is disabled - always allow login
      // Kiểm tra nếu email chưa verified (local account) - DISABLED
      // if (!userData.emailVerified) {
      //   return { success: true, requiresVerification: true }
      // }

      return { success: true }
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Đăng nhập thất bại. Vui lòng thử lại.'
      return {
        success: false,
        error: errorMessage,
      }
    }
  }

  /**
   * Xử lý SSO callback và đăng nhập
   */
  const handleSSOCallback = async (
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
  }

  /**
   * Đăng xuất
   */
  const logout = async (): Promise<void> => {
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
  }

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
  const refreshUser = async (): Promise<void> => {
    try {
      const { authService } = await import('../services/auth.service')
      const userData = await authService.getCurrentUser()
      const { accessToken, refreshToken, userId, ...rest } = userData

      // Normalize roles từ backend
      const normalizedRoles = normalizeRoles(rest.roles)

      // Map userId to id for User interface
      const user: User = {
        id: userId,
        email: rest.email,
        fullName: rest.fullName,
        roles: normalizedRoles,
        emailVerified: rest.emailVerified,
      }
      setUser(user)
      localStorage.setItem('user', JSON.stringify(user))
    } catch (error) {
      console.error('Error refreshing user:', error)
    }
  }

  const value: AuthContextType = {
    user,
    loading,
    isAuthenticated,
    login,
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
