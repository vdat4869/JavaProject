import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  CButton,
  CCard,
  CCardBody,
  CCardGroup,
  CCol,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CRow,
  CAlert,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilLockLocked, cilUser, cilEnvelopeClosed } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'

/**
 * RegisterPage - Trang đăng ký
 *
 * Features:
 * - Email/Password registration (Local account)
 * - Form validation
 * - Error handling
 * - Redirect đến verify email sau khi đăng ký thành công
 */
const RegisterPage: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
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
    if (!formData.email || !formData.password || !formData.fullName) {
      setError(t('auth.allFieldsRequired') || 'Vui lòng điền đầy đủ thông tin')
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
      await authService.register({
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName,
      })

      // Redirect đến verify email page
      navigate('/verify-email', {
        state: { email: formData.email, message: t('auth.registrationSuccess') },
      })
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.registrationFailed') || 'Đăng ký thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <CRow className="justify-content-center">
      <CCol md={8}>
        <CCardGroup>
          <CCard className="p-4">
            <CCardBody>
              <CForm onSubmit={handleSubmit}>
                <h1>{t('common.register') || 'Đăng ký'}</h1>
                <p className="text-body-secondary">
                  {t('auth.registerTitle') || 'Tạo tài khoản mới'}
                </p>
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
                    type="text"
                    name="fullName"
                    placeholder={t('common.fullName') || 'Họ và tên'}
                    autoComplete="name"
                    value={formData.fullName}
                    onChange={handleChange}
                    required
                  />
                </CInputGroup>
                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilEnvelopeClosed} />
                  </CInputGroupText>
                  <CFormInput
                    type="email"
                    name="email"
                    placeholder={t('common.email') || 'Email'}
                    autoComplete="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                </CInputGroup>
                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilLockLocked} />
                  </CInputGroupText>
                  <CFormInput
                    type="password"
                    name="password"
                    placeholder={t('common.password') || 'Mật khẩu'}
                    autoComplete="new-password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                  />
                </CInputGroup>
                <CInputGroup className="mb-4">
                  <CInputGroupText>
                    <CIcon icon={cilLockLocked} />
                  </CInputGroupText>
                  <CFormInput
                    type="password"
                    name="confirmPassword"
                    placeholder={t('auth.confirmPassword') || 'Xác nhận mật khẩu'}
                    autoComplete="new-password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    required
                  />
                </CInputGroup>
                <div className="d-grid">
                  <CButton color="primary" className="px-4" type="submit" disabled={loading}>
                    {loading
                      ? t('common.loading') || 'Đang xử lý...'
                      : t('common.register') || 'Đăng ký'}
                  </CButton>
                </div>
                <CRow className="mt-3">
                  <CCol className="text-center">
                    <span className="text-body-secondary">
                      {t('auth.alreadyHaveAccount') || 'Đã có tài khoản?'}{' '}
                      <Link to="/login">{t('common.login') || 'Đăng nhập'}</Link>
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

export default RegisterPage
