import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  CButton,
  CCard,
  CCardBody,
  CCol,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CRow,
  CAlert,
  CSpinner,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilLockLocked, cilUser, cilArrowRight } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../context/AuthContext'

// Custom style for placeholders
const inputGlobalStyle = `
  .custom-input::placeholder {
    color: #666 !important;
    opacity: 0.8;
  }
`;


// UTH Logo - Full version for header
import uthLogoFull from '../../assets/images/idrV1VcT-T_logos.jpeg'

/**
 * LoginPage - Trang đăng nhập
 * 
 * Styled to match Portal UTH aesthetic:
 * - Teal primary color (#008585)
 * - Red title text
 * - Clean white card
 */
const LoginPage: React.FC = () => {
  const { t } = useTranslation()
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const result = await login(email, password)
      if (result.success) {
        navigate('/app')
      } else {
        setError(result.error || t('auth.loginFailed'))
      }
    } catch (err: any) {
      setError(err.message || t('common.error'))
    } finally {
      setLoading(false)
    }
  }

  const handleSSOLogin = () => {
    navigate('/auth/sso/redirect')
  }

  const colors = {
    teal: '#008585',
    red: '#b31d1d',
    border: '#abb5be', // Darker border
    text: '#212529' // Standard dark text
  }

  const styles = {
    card: {
      borderRadius: '8px',
      border: 'none',
      boxShadow: '0 10px 40px rgba(0,0,0,0.25)',
      backgroundColor: '#fff',
      padding: '30px 50px',
      maxWidth: '550px', // Wider
      width: '100%'
    },
    logoHeader: {
      textAlign: 'center' as const,
      marginBottom: '5px', // Minimal spacing
      marginTop: '-15px', // Pull up
      overflow: 'hidden',
      maxHeight: '120px' // Clip any excessive white space
    },
    logoImage: {
      maxWidth: '240px',
      height: 'auto',
      display: 'inline-block'
    },
    loginTitle: {
      color: colors.red,
      fontSize: '1.5rem',
      fontWeight: 700,
      textAlign: 'center' as const,
      marginBottom: '20px',
      textTransform: 'uppercase' as const,
      letterSpacing: '1px'
    },
    inputGroup: {
      marginBottom: '16px'
    },
    input: {
      backgroundColor: '#fff', // White background for best contrast
      border: `1px solid ${colors.border}`,
      borderRadius: '4px',
      padding: '14px 18px',
      fontSize: '1rem',
      color: colors.text
    },
    btnSubmit: {
      backgroundColor: colors.teal,
      borderColor: colors.teal,
      color: '#fff',
      fontWeight: 'bold',
      padding: '14px',
      borderRadius: '4px',
      marginTop: '10px',
      letterSpacing: '1px'
    },
    forgotLink: {
      color: colors.teal,
      textDecoration: 'none',
      fontSize: '0.95rem',
      display: 'block',
      textAlign: 'right' as const,
      marginTop: '5px',
      fontWeight: 600
    },
    divider: {
      display: 'flex',
      alignItems: 'center',
      margin: '12px 0', // Compact
    },
    dividerLine: {
      flex: 1,
      height: '1px',
      backgroundColor: '#dee2e6',
    },
    dividerText: {
      padding: '0 1rem',
      color: '#495057',
      fontSize: '0.9rem',
      fontWeight: 500
    },
    ssoButton: {
      backgroundColor: '#fff',
      border: `1px solid ${colors.border}`,
      color: '#333',
      padding: '12px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      borderRadius: '4px',
      fontSize: '1rem',
      fontWeight: 500
    },
    registerText: {
      textAlign: 'center' as const,
      marginTop: '20px',
      fontSize: '1rem',
      color: '#495057'
    }
  }

  return (
    <CRow className="justify-content-end align-items-center min-vh-100 pe-md-5 me-md-5">
      <style>{inputGlobalStyle}</style>
      <CCol xs={12} sm={10} md={8} lg={6} xl={5} className="d-flex justify-content-end pe-lg-5">
        <CCard style={styles.card}>
          <CCardBody className="p-0">
            {/* Logo */}
            <div style={styles.logoHeader}>
              <img src={uthLogoFull} alt="UTH Logo" style={styles.logoImage} />
            </div>

            {/* Title */}
            <h2 style={styles.loginTitle}>Đăng nhập hệ thống</h2>

            <CForm onSubmit={handleSubmit}>
              {error && (
                <CAlert color="danger" className="mb-3">
                  {error}
                </CAlert>
              )}

              {/* Email / Username */}
              <div className="mb-3">
                <CFormInput
                  type="email"
                  placeholder="Tài khoản đăng nhập"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  style={styles.input}
                  className="custom-input"
                  required
                />
              </div>

              {/* Password */}
              <div className="mb-2">
                <CFormInput
                  type="password"
                  placeholder="Mật khẩu"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  style={styles.input}
                  className="custom-input"
                  required
                />
              </div>

              {/* Login Button */}
              <div className="d-grid gap-2">
                <CButton type="submit" style={styles.btnSubmit} disabled={loading}>
                  {loading ? <CSpinner size="sm" /> : 'ĐĂNG NHẬP'}
                </CButton>
              </div>

              {/* Forgot Password */}
              <Link to="/forgot-password" style={styles.forgotLink}>
                QUÊN MẬT KHẨU?
              </Link>

              {/* Divider */}
              <div style={styles.divider}>
                <div style={styles.dividerLine}></div>
                <span style={styles.dividerText}>HOẶC</span>
                <div style={styles.dividerLine}></div>
              </div>

              {/* SSO Button */}
              <CButton style={{ ...styles.ssoButton, marginTop: '0' }} onClick={handleSSOLogin} className="w-100 shadow-sm">
                <svg width="20" height="20" className="me-2" viewBox="0 0 48 48">
                  <path fill="#FFC107" d="M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z" />
                  <path fill="#FF3D00" d="M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z" />
                  <path fill="#4CAF50" d="M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z" />
                  <path fill="#1976D2" d="M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571c0.001-0.001,0.002-0.001,0.003-0.002l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z" />
                </svg>
                {t('auth.loginWithSSO')}
              </CButton>

              {/* Register */}
              <div style={styles.registerText}>
                {t('auth.dontHaveAccount')}{' '}
                <Link to="/register" style={{ color: colors.teal, fontWeight: 700, textDecoration: 'none' }}>
                  {t('common.register')}
                </Link>
              </div>
            </CForm>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  )
}

export default LoginPage
