import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CAlert, CButton, CSpinner } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'
import { useAuth } from '../../context/AuthContext'

/**
 * EmailVerificationPage - Trang xác thực email
 *
 * Features:
 * - Verify email với token từ URL
 * - Hiển thị trạng thái verification
 * - Redirect sau khi verify thành công
 * - Refresh user data sau khi verify
 */
const EmailVerificationPage: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { refreshUser } = useAuth()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [message, setMessage] = useState('')

  const verifyEmail = React.useCallback(
    async function (token: string) {
      try {
        await authService.verifyEmail(token)
        setStatus('success')
        setMessage(t('auth.emailVerified'))

        // Refresh user data để cập nhật emailVerified status
        await refreshUser()

        // Redirect to app sau 2 giây
        setTimeout(() => {
          navigate('/app')
        }, 2000)
      } catch (error: any) {
        setStatus('error')
        setMessage(error.response?.data?.message || t('auth.verificationFailed'))
      }
    },
    [refreshUser, navigate, t],
  )

  useEffect(() => {
    ;(async () => {
      const token = searchParams.get('token')
      if (token) {
        await verifyEmail(token)
      } else {
        setStatus('error')
        setMessage(t('auth.invalidToken'))
      }
    })()
  }, [searchParams])

  return (
    <CCard>
      <CCardHeader>
        <h4>{t('auth.emailVerification')}</h4>
      </CCardHeader>
      <CCardBody>
        {status === 'loading' && (
          <div className="text-center">
            <CSpinner color="primary" />
            <p className="mt-3">{t('auth.verifying')}</p>
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
            <CButton color="primary" onClick={() => navigate('/login')}>
              {t('common.backToLogin')}
            </CButton>
          </>
        )}
      </CCardBody>
    </CCard>
  )
}

export default EmailVerificationPage
