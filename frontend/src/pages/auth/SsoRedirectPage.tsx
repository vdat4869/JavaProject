import React, { useEffect, useState } from 'react'
import { CCard, CCardBody, CCardHeader, CAlert, CSpinner } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'

/**
 * SsoRedirectPage - Trang redirect đến SSO provider
 *
 * Features:
 * - Tự động lấy SSO redirect URL từ API
 * - Redirect user đến SSO provider
 * - Hiển thị loading state
 */
const SsoRedirectPage: React.FC = () => {
  const { t } = useTranslation()
  const [error, setError] = useState<string>('')
  const [loading, setLoading] = useState<boolean>(true)

  const redirectToSSO = React.useCallback(async () => {
    try {
      setLoading(true)
      const redirectUrl = await authService.getSSORedirectUrl()

      // Redirect đến SSO provider
      window.location.href = redirectUrl
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.ssoRedirectFailed'))
      setLoading(false)
    }
  }, [t])

  useEffect(() => {
    ;(async () => {
      await redirectToSSO()
    })()
  }, [redirectToSSO])

  if (loading) {
    return (
      <CCard>
        <CCardHeader>
          <h4>{t('auth.ssoRedirect')}</h4>
        </CCardHeader>
        <CCardBody>
          <div className="text-center">
            <CSpinner color="primary" />
            <p className="mt-3">{t('auth.redirectingToSSO')}</p>
          </div>
        </CCardBody>
      </CCard>
    )
  }

  if (error) {
    return (
      <CCard>
        <CCardHeader>
          <h4>{t('auth.ssoRedirect')}</h4>
        </CCardHeader>
        <CCardBody>
          <CAlert color="danger">{error}</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return null
}

export default SsoRedirectPage
