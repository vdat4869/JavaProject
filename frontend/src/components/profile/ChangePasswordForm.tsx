import React, { useState } from 'react'
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CAlert,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilLockLocked } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { authService } from '../../services/auth.service'

/**
 * ChangePasswordForm - Form để đổi mật khẩu
 */
const ChangePasswordForm: React.FC = () => {
  const { t } = useTranslation()
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    setError('')
    setSuccess('')
  }

  const validateForm = (): boolean => {
    if (!formData.currentPassword || !formData.newPassword || !formData.confirmPassword) {
      setError(t('auth.allFieldsRequired') || 'Vui lòng điền đầy đủ thông tin')
      return false
    }

    if (formData.newPassword.length < 8) {
      setError(t('auth.passwordMinLength') || 'Mật khẩu mới phải có ít nhất 8 ký tự')
      return false
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setError(t('auth.passwordMismatch') || 'Mật khẩu xác nhận không khớp')
      return false
    }

    if (formData.currentPassword === formData.newPassword) {
      setError(t('auth.passwordSameAsCurrent') || 'Mật khẩu mới phải khác mật khẩu hiện tại')
      return false
    }

    return true
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (!validateForm()) {
      return
    }

    setLoading(true)

    try {
      await authService.changePassword({
        currentPassword: formData.currentPassword,
        newPassword: formData.newPassword,
      })

      setSuccess(t('auth.passwordChangedSuccess') || 'Đổi mật khẩu thành công')
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      })
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          t('auth.passwordChangeFailed') ||
          'Đổi mật khẩu thất bại',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <CCard>
      <CCardHeader>
        <h5>{t('auth.changePassword') || 'Đổi mật khẩu'}</h5>
      </CCardHeader>
      <CCardBody>
        <CForm onSubmit={handleSubmit}>
          {error && (
            <CAlert color="danger" className="mb-3">
              {error}
            </CAlert>
          )}
          {success && (
            <CAlert color="success" className="mb-3">
              {success}
            </CAlert>
          )}

          <CInputGroup className="mb-3">
            <CInputGroupText>
              <CIcon icon={cilLockLocked} />
            </CInputGroupText>
            <CFormInput
              type="password"
              name="currentPassword"
              placeholder={t('auth.currentPassword') || 'Mật khẩu hiện tại'}
              autoComplete="current-password"
              value={formData.currentPassword}
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
              name="newPassword"
              placeholder={t('auth.newPassword') || 'Mật khẩu mới'}
              autoComplete="new-password"
              value={formData.newPassword}
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
            <CButton color="primary" type="submit" disabled={loading}>
              {loading ? t('common.loading') || 'Đang xử lý...' : t('auth.changePassword') || 'Đổi mật khẩu'}
            </CButton>
          </div>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default ChangePasswordForm
