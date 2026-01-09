import React, { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth, UserRole } from '../context/AuthContext'
import { CSpinner } from '@coreui/react'

/**
 * RouteGuard Props
 */
interface RouteGuardProps {
  children: ReactNode
  allowedRoles?: UserRole[]
  requireAuth?: boolean
}

/**
 * RouteGuard - Component để bảo vệ routes
 *
 * Features:
 * - Kiểm tra authentication
 * - Kiểm tra roles
 * - Redirect nếu không có quyền
 */
export const RouteGuard: React.FC<RouteGuardProps> = ({
  children,
  allowedRoles,
  requireAuth = true,
}) => {
  const { isAuthenticated, loading, hasAnyRole } = useAuth()

  // Hiển thị loading khi đang check auth
  if (loading) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: '100vh' }}
      >
        <CSpinner color="primary" />
      </div>
    )
  }

  // Nếu route yêu cầu auth nhưng user chưa đăng nhập
  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  // Nếu route yêu cầu roles cụ thể
  if (allowedRoles && allowedRoles.length > 0) {
    if (!hasAnyRole(allowedRoles)) {
      // Redirect to 403 hoặc dashboard
      return <Navigate to="/app" replace />
    }
  }

  return <>{children}</>
}
