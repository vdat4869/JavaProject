import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  CButton,
  CCard,
  CCardBody,
  CCol,
  CForm,
  CFormInput,
  CRow,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'
import { useAuth } from '../../context/AuthContext'
import OrganizationSelect from '../../components/common/OrganizationSelect'

// UTH Logo - Full version for header
import uthLogoFull from '../../assets/images/idrV1VcT-T_logos.jpeg'

/**
 * RegisterPage - Trang đăng ký
 * 
 * Styled to match Portal UTH aesthetic:
 * - Teal primary color (#008585)
 * - Red title text
 * - Clean white card
 * - Right-aligned layout
 */
const RegisterPage: React.FC = () => {
  const { t } = useTranslation()
  const { loginWithTokens } = useAuth()
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    organizationId: null as number | null,
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    setError('')
  }

  const validateForm = (): boolean => {
    if (!formData.email || !formData.password || !formData.firstName || !formData.lastName || !formData.organizationId) {
      setError(t('auth.allFieldsRequired') || 'Vui lòng điền đầy đủ thông tin, bao gồm cả đơn vị')
      return false
    }

    if (formData.password.length < 8) {
      setError(t('auth.passwordMinLength') || 'Mật khẩu phải có ít nhất 8 ký tự')
      return false
    }

    if (formData.password !== formData.confirmPassword) {
      setError(t('auth.passwordMismatch') || 'Mật khẩu xác nhận không khớp')
      return false
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(formData.email)) {
      setError(t('auth.invalidEmail') || 'Email không hợp lệ')
      return false
    }

    return true
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!validateForm()) {
      return
    }

    setLoading(true)

    try {
      const response = await authService.register({
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName,
        organizationId: formData.organizationId,
      })

      // Auto-login after registration
      if (response.accessToken && response.refreshToken) {
        await loginWithTokens(response.accessToken, response.refreshToken)
        navigate('/app')
      } else {
        // Fallback to verification page if no tokens (though backend returns them)
        navigate('/verify-email', {
          state: { email: formData.email, message: t('auth.registrationSuccess') },
        })
      }
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.registrationFailed') || 'Đăng ký thất bại')
    } finally {
      setLoading(false)
    }
  }

  const colors = {
    teal: '#008585',
    red: '#b31d1d',
    border: '#abb5be',
    text: '#212529'
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
    },
    input: {
      backgroundColor: '#fff',
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
    footerText: {
      textAlign: 'center' as const,
      marginTop: '20px',
      fontSize: '1rem',
      color: '#495057'
    }
  }

  // Custom style for placeholders
  const inputGlobalStyle = `
    .custom-input::placeholder {
      color: #666 !important;
      opacity: 0.8;
    }
  `;

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
            <h2 style={styles.title}>Đăng ký tài khoản</h2>

            <CForm onSubmit={handleSubmit}>
              {error && (
                <CAlert color="danger" className="mb-3">
                  {error}
                </CAlert>
              )}

              {/* Name fields row */}
              <CRow className="mb-3">
                <CCol md={7}>
                  <CFormInput
                    type="text"
                    name="firstName"
                    placeholder={t('common.firstName') || 'Họ và tên đệm'}
                    value={formData.firstName}
                    onChange={handleChange}
                    style={styles.input}
                    className="custom-input"
                    required
                  />
                </CCol>
                <CCol md={5}>
                  <CFormInput
                    type="text"
                    name="lastName"
                    placeholder={t('common.lastName') || 'Tên'}
                    value={formData.lastName}
                    onChange={handleChange}
                    style={styles.input}
                    className="custom-input"
                    required
                  />
                </CCol>
              </CRow>

              <div className="mb-3">
                <OrganizationSelect
                  value={formData.organizationId || undefined}
                  onChange={(id) => setFormData(prev => ({ ...prev, organizationId: id }))}
                  style={styles.input}
                  placeholder={t('common.organization') || 'Chọn đơn vị công tác'}
                />
              </div>

              {/* Email */}
              <div className="mb-3">
                <CFormInput
                  type="email"
                  name="email"
                  placeholder={t('common.email') || 'Email'}
                  value={formData.email}
                  onChange={handleChange}
                  style={styles.input}
                  className="custom-input"
                  required
                />
              </div>

              {/* Password */}
              <div className="mb-3">
                <CFormInput
                  type="password"
                  name="password"
                  placeholder={t('common.password') || 'Mật khẩu'}
                  value={formData.password}
                  onChange={handleChange}
                  style={styles.input}
                  className="custom-input"
                  required
                />
              </div>

              {/* Confirm Password */}
              <div className="mb-4">
                <CFormInput
                  type="password"
                  name="confirmPassword"
                  placeholder={t('auth.confirmPassword') || 'Xác nhận mật khẩu'}
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  style={styles.input}
                  className="custom-input"
                  required
                />
              </div>

              {/* Submit Button */}
              <div className="d-grid gap-2">
                <CButton type="submit" style={styles.btnSubmit} disabled={loading}>
                  {loading ? (
                    <CSpinner size="sm" />
                  ) : (
                    t('common.register') || 'ĐĂNG KÝ'
                  )}
                </CButton>
              </div>

              {/* Already Have Account */}
              <div style={styles.footerText}>
                {t('auth.alreadyHaveAccount')}{' '}
                <Link to="/login" style={{ color: colors.teal, fontWeight: 700, textDecoration: 'none' }}>
                  {t('common.login')}
                </Link>
              </div>
            </CForm>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  )
}

export default RegisterPage
