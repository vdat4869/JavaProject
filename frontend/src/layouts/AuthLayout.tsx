import React, { ReactNode } from 'react'
import { CContainer } from '@coreui/react'

// Import background image
import uthBackground from '../assets/images/uth-background.jpg'

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
 * Layout với background image trường UTH
 */
const AuthLayout: React.FC<AuthLayoutProps> = ({ children }) => {
  return (
    <div
      className="min-vh-100 d-flex flex-row align-items-center"
      style={{
        backgroundImage: `linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url(${uthBackground})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundAttachment: 'fixed',
      }}
    >
      <CContainer>{children}</CContainer>
    </div>
  )
}

export default AuthLayout
