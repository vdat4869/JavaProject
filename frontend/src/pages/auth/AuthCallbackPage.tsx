import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CAlert,
  CButton,
  CSpinner,
  CRow,
  CCol,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../context/AuthContext'
import uthLogoFull from '../../assets/images/idrV1VcT-T_logos.jpeg'
// SSO callback handler

/**
 * AuthCallbackPage - Trang xử lý SSO callback
 */
const AuthCallbackPage: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { handleSSOCallback, loginWithTokens } = useAuth()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    let isProcessing = false

    const processCallback = async () => {
      if (isProcessing) return
      isProcessing = true

      const code = searchParams.get('code')
      const state = searchParams.get('state')
      const error = searchParams.get('error')
      const token = searchParams.get('token')
      const refreshToken = searchParams.get('refreshToken')

      if (error) {
        setStatus('error')
        setMessage(t('auth.ssoError') || `SSO Error: ${error}`)
        return
      }

      if (token && refreshToken) {
        try {
          const result = await loginWithTokens(token, refreshToken)
          if (result.success) {
            setStatus('success')
            setMessage(t('auth.ssoSuccess') || 'Đăng nhập thành công!')
            setTimeout(() => navigate('/app/dashboard'), 1000)
          } else {
            setStatus('error')
            setMessage(result.error || t('auth.ssoFailed') || 'SSO failed')
          }
        } catch (err: any) {
          setStatus('error')
          setMessage(err?.message || t('auth.ssoFailed') || 'SSO error')
        }
        return
      }

      if (code) {
        try {
          const result = await handleSSOCallback(code, state)
          if (result.success) {
            setStatus('success')
            setMessage(t('auth.ssoSuccess') || 'Đăng nhập thành công!')
            setTimeout(() => navigate('/app/dashboard'), 1000)
          } else {
            setStatus('error')
            setMessage(result.error || t('auth.ssoFailed') || 'SSO failed')
          }
        } catch (err: any) {
          setStatus('error')
          setMessage(err?.message || t('auth.ssoFailed') || 'SSO error')
        }
        return
      }

      setStatus('error')
      setMessage(t('auth.invalidSSOCode') || 'Invalid SSO response')
    }

    processCallback()
  }, [searchParams, handleSSOCallback, loginWithTokens, navigate, t])

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
            <h2 style={styles.title}>{t('auth.ssoCallback') || 'XÁC THỰC SSO'}</h2>

            <div className="py-4">
              {status === 'loading' && (
                <div className="text-center">
                  <CSpinner style={{ color: colors.teal }} />
                  <p className="mt-3 text-muted">{t('auth.processingSSO') || 'Đang xử lý đăng nhập...'}</p>
                </div>
              )}
              {status === 'success' && (
                <CAlert color="success" className="text-center py-3">
                  <div className="mb-2">✅ {message}</div>
                  <small className="text-muted">{t('auth.redirectingToApp') || 'Đang chuyển hướng...'}</small>
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

export default AuthCallbackPage
