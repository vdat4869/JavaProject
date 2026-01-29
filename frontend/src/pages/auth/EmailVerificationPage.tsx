import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate, useLocation } from 'react-router-dom'
import { CCard, CCardBody, CAlert, CButton, CSpinner, CRow, CCol } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'
import { useAuth } from '../../context/AuthContext'
import uthLogoFull from '../../assets/images/idrV1VcT-T_logos.jpeg'

/**
 * EmailVerificationPage - Trang xác thực email
 */
const EmailVerificationPage: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { refreshUser, isAuthenticated } = useAuth()
  const location = useLocation()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  const verifyEmail = React.useCallback(
    async function (token: string) {
      try {
        await authService.verifyEmail(token)
        setStatus('success')
        setMessage(t('auth.emailVerified') || 'Xác thực email thành công!')

        // Refresh user data để cập nhật emailVerified status
        await refreshUser()

        // Redirect to app sau 2 giây
        setTimeout(() => {
          navigate('/app')
        }, 2000)
      } catch (error: any) {
        setStatus('error')
        setMessage(error.response?.data?.message || t('auth.verificationFailed') || 'Xác thực không thành công hoặc liên kết đã hết hạn.')
      }
    },
    [refreshUser, navigate, t],
  )

  useEffect(() => {
    const token = searchParams.get('token')
    const state = location.state as any
    const stateMessage = state?.message

    if (token) {
      verifyEmail(token)
    } else if (stateMessage) {
      // Nếu được redirect từ RegisterPage với message thành công
      setStatus('success')
      setMessage(stateMessage)

      // Nếu đã authenticated thì sau 3s tự redirect về dashboard
      if (isAuthenticated) {
        setTimeout(() => navigate('/app'), 3000)
      }
    } else if (isAuthenticated) {
      // Nếu đã đăng nhập rồi mà vào đây không có token thì về dashboard
      navigate('/app')
    } else {
      setStatus('error')
      setMessage(t('auth.invalidToken') || 'Token không hợp lệ.')
    }
  }, [searchParams, verifyEmail, t, location.state, isAuthenticated, navigate])

  const colors = {
    teal: '#008585',
    red: '#b31d1d',
    border: '#abb5be'
  }

  const styles = {
    card: {
      borderRadius: '8px',
      border: 'none',
      boxShadow: '0 10px 40px rgba(0,0,0,0.25)',
      backgroundColor: '#fff',
      padding: '30px 50px',
      maxWidth: '550px',
      width: '100%'
    },
    logoHeader: {
      textAlign: 'center' as const,
      marginBottom: '5px',
      marginTop: '-15px',
      overflow: 'hidden',
      maxHeight: '120px'
    },
    logoImage: {
      maxWidth: '240px',
      height: 'auto',
      display: 'inline-block'
    },
    title: {
      color: colors.red,
      fontSize: '1.5rem',
      fontWeight: 700,
      textAlign: 'center' as const,
      marginBottom: '20px',
      textTransform: 'uppercase' as const,
      letterSpacing: '1px'
    }
  }

  return (
    <CRow className="justify-content-end align-items-center min-vh-100 pe-md-5 me-md-5">
      <CCol xs={12} sm={10} md={8} lg={6} xl={5} className="d-flex justify-content-end pe-lg-5">
        <CCard style={styles.card}>
          <CCardBody className="p-0">
            {/* Logo */}
            <div style={styles.logoHeader}>
              <img src={uthLogoFull} alt="UTH Logo" style={styles.logoImage} />
            </div>

            {/* Title */}
            <h2 style={styles.title}>{t('auth.emailVerification') || 'XÁC THỰC EMAIL'}</h2>

            <div className="py-4">
              {status === 'loading' && (
                <div className="text-center">
                  <CSpinner style={{ color: colors.teal }} />
                  <p className="mt-3 text-muted">{t('auth.verifying') || 'Đang xác thực email của bạn...'}</p>
                </div>
              )}
              {status === 'success' && (
                <CAlert color="success" className="text-center">
                  <div className="mb-2">✅ {message}</div>
                  <small className="text-muted">{t('auth.redirectingToApp') || 'Đang chuyển hướng đến ứng dụng...'}</small>
                </CAlert>
              )}
              {status === 'error' && (
                <div className="text-center">
                  <CAlert color="danger" className="text-start">{message}</CAlert>
                  <CButton
                    style={{ backgroundColor: colors.teal, borderColor: colors.teal, color: '#fff' }}
                    onClick={() => navigate('/login')}
                    className="mt-3"
                  >
                    {t('common.backToLogin') || 'Quay lại đăng nhập'}
                  </CButton>
                </div>
              )}
            </div>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  )
}

export default EmailVerificationPage
