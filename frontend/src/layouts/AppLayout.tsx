import React from 'react'
import { Outlet, Navigate } from 'react-router-dom'
import { AppSidebar, AppHeader, AppFooter, AppContent } from '../components'
import { useAuth } from '../context/AuthContext'
import { RouteGuard } from '../routes/RouteGuard'

/**
 * AppLayout - Layout chính cho ứng dụng sau khi đăng nhập
 *
 * Layout bao gồm:
 * - Sidebar navigation
 * - Header với user dropdown
 * - Main content area
 * - Footer
 */
const AppLayout: React.FC = () => {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return (
      <div
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: '100vh' }}
      >
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return (
    <RouteGuard requireAuth={true}>
      <div>
        <AppSidebar />
        <div className="wrapper d-flex flex-column min-vh-100">
          <AppHeader />
          <div className="body flex-grow-1">
            <AppContent>
              <Outlet />
            </AppContent>
          </div>
          <AppFooter />
        </div>
      </div>
    </RouteGuard>
  )
}

export default AppLayout
