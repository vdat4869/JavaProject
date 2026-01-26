import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CAlert,
  CButton,
  CSpinner,
  CContainer,
  CRow,
  CCol,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../context/AuthContext'

/**
 * AuthCallbackPage - Trang xử lý SSO callback
 *
 * Features:
 * - Xử lý OAuth callback từ SSO provider
 * - Exchange code for tokens
 * - Auto login sau khi SSO thành công
 * - Redirect về app hoặc verify email page nếu cần
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

      console.log('SSO Callback processing...', { hasToken: !!token, hasCode: !!code })

      if (error) {
        console.error('SSO Error from URL:', error)
        setStatus('error')
        setMessage(t('auth.ssoError') || `SSO Error: ${error}`)
        return
      }

      // 1. JWT Tokens directly in URL (Spring Security SuccessHandler Flow)
      if (token && refreshToken) {
        try {
          console.log('Attempting login with tokens...')
          const result = await loginWithTokens(token, refreshToken)
          if (result.success) {
            console.log('Login successful, navigating to app...')
            setStatus('success')
            setMessage(t('auth.ssoSuccess') || 'Logged in successfully!')
            setTimeout(() => navigate('/app/dashboard'), 1000)
          } else {
            console.error('Login with tokens failed:', result.error)
            setStatus('error')
            setMessage(result.error || t('auth.ssoFailed') || 'SSO failed')
          }
        } catch (err: any) {
          console.error('Exception during loginWithTokens:', err)
          setStatus('error')
          setMessage(err?.message || t('auth.ssoFailed') || 'SSO error')
        }
        return
      }

      // 2. Auth Code in URL (Standard/Manual Flow)
      if (code) {
        try {
          console.log('Attempting login with code...')
          const result = await handleSSOCallback(code, state)
          if (result.success) {
            console.log('Login successful (code), navigating to app...')
            setStatus('success')
            setMessage(t('auth.ssoSuccess') || 'Logged in successfully!')
            setTimeout(() => navigate('/app/dashboard'), 1000)
          } else {
            console.error('Login with code failed:', result.error)
            setStatus('error')
            setMessage(result.error || t('auth.ssoFailed') || 'SSO failed')
          }
        } catch (err: any) {
          console.error('Exception during handleSSOCallback:', err)
          setStatus('error')
          setMessage(err?.message || t('auth.ssoFailed') || 'SSO error')
        }
        return
      }

      // 3. Neither tokens nor code
      console.warn('Neither token nor code found in URL params')
      setStatus('error')
      setMessage(t('auth.invalidSSOCode') || 'Invalid SSO response')
    }

    processCallback()
  }, [searchParams, handleSSOCallback, loginWithTokens, navigate, t])

  return (
    <div className="bg-light min-vh-100 d-flex flex-row align-items-center">
      <CContainer>
        <CRow className="justify-content-center">
          <CCol md={6}>
            <CCard className="p-4 shadow">
              <CCardHeader className="bg-white border-bottom-0 pb-0">
                <h4 className="text-center">{t('auth.ssoCallback') || 'Authenticating...'}</h4>
              </CCardHeader>
              <CCardBody>
                {status === 'loading' && (
                  <div className="text-center py-4">
                    <CSpinner color="primary" />
                    <p className="mt-3 text-muted">{t('auth.processingSSO') || 'Processing...'}</p>
                  </div>
                )}
                {status === 'success' && (
                  <CAlert color="success" className="text-center py-3">
                    <div className="mb-2">✅ {message}</div>
                    <small className="text-muted">{t('auth.redirectingToApp') || 'Redirecting...'}</small>
                  </CAlert>
                )}
                {status === 'error' && (
                  <div className="text-center">
                    <CAlert color="danger">{message}</CAlert>
                    <CButton color="primary" className="mt-3" onClick={() => navigate('/login')}>
                      {t('common.backToLogin') || 'Back to Login'}
                    </CButton>
                  </div>
                )}
              </CCardBody>
            </CCard>
          </CCol>
        </CRow>
      </CContainer>
    </div>
  )
}

export default AuthCallbackPage
