import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  CButton,
  CCard,
  CCardBody,
  CCardGroup,
  CCol,
  CContainer,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CRow,
  CAlert,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilLockLocked, cilUser } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../context/AuthContext'

/**
 * LoginPage - Trang đăng nhập
 *
 * Features:
 * - Email/Password login (Local account)
 * - SSO login button (bắt buộc)
 * - Error handling
 * - Redirect sau khi login thành công
 * - Email verification is disabled
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
        // TODO: Email verification is disabled - always redirect to app
        // Nếu local account chưa verify email, redirect đến verify page - DISABLED
        // if (result.requiresVerification) {
        //   navigate('/verify-email', { state: { email } })
        // } else {
        //   navigate('/app')
        // }
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
    // Redirect đến SSO redirect page
    navigate('/auth/sso/redirect')
  }

  return (
    <CRow className="justify-content-center">
      <CCol md={8}>
        <CCardGroup>
          <CCard className="p-4">
            <CCardBody>
              <CForm onSubmit={handleSubmit}>
                <h1>{t('common.login')}</h1>
                <p className="text-body-secondary">{t('auth.loginTitle')}</p>
                {error && (
                  <CAlert color="danger" className="mb-3">
                    {error}
                  </CAlert>
                )}
                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilUser} />
                  </CInputGroupText>
                  <CFormInput
                    type="email"
                    placeholder={t('common.email')}
                    autoComplete="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </CInputGroup>
                <CInputGroup className="mb-4">
                  <CInputGroupText>
                    <CIcon icon={cilLockLocked} />
                  </CInputGroupText>
                  <CFormInput
                    type="password"
                    placeholder={t('common.password')}
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </CInputGroup>
                <CRow>
                  <CCol xs={6}>
                    <CButton color="primary" className="px-4" type="submit" disabled={loading}>
                      {loading ? t('common.loading') : t('common.login')}
                    </CButton>
                  </CCol>
                  <CCol xs={6} className="text-right">
                    <CButton
                      color="link"
                      className="px-0"
                      onClick={() => navigate('/forgot-password')}
                    >
                      {t('auth.forgotPassword')}
                    </CButton>
                  </CCol>
                </CRow>

                <div className="d-flex align-items-center my-4">
                  <hr className="flex-grow-1" />
                  <span className="px-3 text-muted">{t('auth.or') || 'HOẶC'}</span>
                  <hr className="flex-grow-1" />
                </div>

                <div className="d-grid">
                  <CButton color="secondary" onClick={handleSSOLogin} disabled={loading}>
                    {t('auth.loginWithSSO')}
                  </CButton>
                </div>
                <CRow className="mt-3">
                  <CCol className="text-center">
                    <span className="text-body-secondary">
                      {t('auth.dontHaveAccount') || 'Chưa có tài khoản?'}{' '}
                      <Link to="/register">{t('common.register') || 'Đăng ký'}</Link>
                    </span>
                  </CCol>
                </CRow>
              </CForm>
            </CCardBody>
          </CCard>
        </CCardGroup>
      </CCol>
    </CRow>
  )
}

export default LoginPage
