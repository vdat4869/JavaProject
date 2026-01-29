import React, { useEffect, useState } from 'react'
import {
  CCard,
  CCardBody,
  CRow,
  CCol,
  CAlert,
  CSpinner,
  CButton
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { authService } from '../../services/auth.service'
import uthLogoFull from '../../assets/images/idrV1VcT-T_logos.jpeg'
// SSO redirect handler

/**
 * SsoRedirectPage - Trang redirect đến SSO provider
 */
const SsoRedirectPage: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
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
    redirectToSSO()
  }, [redirectToSSO])

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
            <h2 style={styles.title}>{t('auth.ssoRedirect') || 'XÁC THỰC SSO'}</h2>

            <div className="text-center py-4">
              {loading ? (
                <>
                  <CSpinner style={{ color: colors.teal }} />
                  <p className="mt-3 text-muted">{t('auth.redirectingToSSO') || 'Đang chuyển hướng đến trang đăng nhập tập trung...'}</p>
                </>
              ) : error ? (
                <>
                  <CAlert color="danger" className="text-start">{error}</CAlert>
                  <CButton
                    style={{ backgroundColor: colors.teal, borderColor: colors.teal, color: '#fff' }}
                    onClick={() => navigate('/login')}
                    className="mt-2"
                  >
                    {t('common.backToLogin') || 'Quay lại đăng nhập'}
                  </CButton>
                </>
              ) : null}
            </div>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  )
}

export default SsoRedirectPage
