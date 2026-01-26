
import React from 'react'
import { Navigate } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../context/AuthContext'

/**
 * DashboardPage - Trang dashboard chính
 *
 * Placeholder page cho phase foundation
 */
const DashboardPage: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useAuth()

  // Đối với Author, chuyển hướng về Author Dashboard riêng
  if (user?.roles.includes('AUTHOR') && !user?.roles.includes('ADMIN') && !user?.roles.includes('CHAIR')) {
    return <Navigate to="/app/author" replace />
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>{t('dashboard.title')}</h4>
      </CCardHeader>
      <CCardBody>
        <p>Chào mừng, {user?.fullName || user?.email}!</p>
        <p>
          Đây là trang dashboard. Các tính năng nghiệp vụ sẽ được thêm vào trong các phase tiếp
          theo.
        </p>
      </CCardBody>
    </CCard>
  )
}

export default DashboardPage
