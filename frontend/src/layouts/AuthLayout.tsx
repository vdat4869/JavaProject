import React, { ReactNode } from 'react'
import { CContainer } from '@coreui/react'

/**
 * AuthLayout Props
 */
interface AuthLayoutProps {
  children: ReactNode
}

/**
 * AuthLayout - Layout cho các trang authentication
 * (Login, Register, Verify Email, SSO Callback)
 *
 * Layout đơn giản, không có sidebar hay header
 */
const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <div className="bg-body-tertiary min-vh-100 d-flex flex-row align-items-center">
      <CContainer>{children}</CContainer>
    </div>
  )
}

export default AuthLayout
