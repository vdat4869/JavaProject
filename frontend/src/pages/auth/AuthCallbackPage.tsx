import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CAlert, CButton, CSpinner } from '@coreui/react'
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
  const { handleSSOCallback } = useAuth()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    ;(async () => {
      const code = searchParams.get('code')
      const state = searchParams.get('state')
      const error = searchParams.get('error')

      if (error) {
        setStatus('error')
        setMessage(t('auth.ssoError'))
        return
      }

      if (code) {
        try {
          const result = await handleSSOCallback(code, state)
          if (result.success) {
            setStatus('success')
            setMessage(t('auth.ssoSuccess'))
            // SSO accounts are considered email verified, redirect to app
            setTimeout(() => {
              navigate('/app')
            }, 1500)
          } else {
            setStatus('error')
            setMessage(result.error || t('auth.ssoFailed'))
          }
        } catch (err: any) {
          setStatus('error')
          setMessage(err?.message || t('auth.ssoFailed'))
        }
      } else {
        setStatus('error')
        setMessage(t('auth.invalidSSOCode'))
      }
    })()
  }, [searchParams, handleSSOCallback, navigate, t])

  return (
    <CCard>
      <CCardHeader>
        <h4>{t('auth.ssoCallback')}</h4>
      </CCardHeader>
      <CCardBody>
        {status === 'loading' && (
          <div className="text-center">
            <CSpinner color="primary" />
            <p className="mt-3">{t('auth.processingSSO')}</p>
          </div>
        )}
        {status === 'success' && (
          <CAlert color="success">
            {message}
            <br />
            <small>{t('auth.redirectingToApp')}</small>
          </CAlert>
        )}
        {status === 'error' && (
          <>
            <CAlert color="danger">{message}</CAlert>
            <CButton color="primary" className="mt-3" onClick={() => navigate('/login')}>
              {t('common.backToLogin')}
            </CButton>
          </>
        )}
      </CCardBody>
    </CCard>
  )
}

export default AuthCallbackPage
