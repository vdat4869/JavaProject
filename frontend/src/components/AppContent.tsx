import React, { ReactNode } from 'react'
import { CContainer } from '@coreui/react'

/**
 * AppContent Props
 */
interface AppContentProps {
  children: ReactNode
}

/**
 * AppContent - Container cho main content area
 * Sử dụng Outlet từ React Router để render child routes
 */
const AppContent: React.FC<AppContentProps> = ({ children }) => {
  return (
    <CContainer className="px-4" lg>
      {children}
    </CContainer>
  )
}

export default React.memo(AppContent)
