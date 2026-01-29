import React, { useState, useEffect } from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CInputGroup,
  CInputGroupText,
  CButton,
  CAlert,
  CTabs,
  CNav,
  CNavItem,
  CNavLink,
  CTabContent,
  CTabPane,
  CSpinner,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import {
  cilUser,
  cilEnvelopeClosed,
  cilPhone,
  cilBuilding,
  cilCheckCircle,
  cilXCircle,
} from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { userService, UserDTO } from '../../services/user.service'
import OrganizationSelect from '../../components/common/OrganizationSelect'
import ChangePasswordForm from '../../components/profile/ChangePasswordForm'

/**
 * ProfilePage - Trang quản lý profile của user
 *
 * Features:
 * - Xem và chỉnh sửa thông tin profile
 * - Đổi mật khẩu
 */
const ProfilePage: React.FC = () => {
  const { t } = useTranslation()
  const [user, setUser] = useState<UserDTO | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [activeTab, setActiveTab] = useState<string>('profile')

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    organizationId: null as number | null,
    organizationName: '',
    phone: '',
  })

  useEffect(() => {
    loadUserProfile()
  }, [])

  const loadUserProfile = async () => {
    try {
      setLoading(true)
      const userData = await userService.getCurrentUser()
      setUser(userData)
      setFormData({
        firstName: userData.firstName || '',
        lastName: userData.lastName || '',
        email: userData.email || '',
        organizationId: userData.organizationId || null,
        organizationName: userData.organizationName || '',
        phone: userData.phone || '',
      })
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi tải thông tin')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    setError('')
    setSuccess('')
  }

  const handleOrganizationChange = (id: number, name: string) => {
    setFormData((prev) => ({
      ...prev,
      organizationId: id,
      organizationName: name,
    }))
    setError('')
    setSuccess('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)

    try {
      const updatedUser = await userService.updateCurrentUser({
        firstName: formData.firstName,
        lastName: formData.lastName,
        organizationId: formData.organizationId || undefined,
        phone: formData.phone,
      })
      setUser(updatedUser)
      setSuccess(t('profile.updateSuccess') || 'Cập nhật thông tin thành công')
    } catch (err: any) {
      setError(err.response?.data?.message || t('profile.updateFailed') || 'Cập nhật thất bại')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!user) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{t('profile.userNotFound') || 'Không tìm thấy thông tin user'}</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <div>
      <h2 className="mb-4">{t('profile.title') || 'Thông tin cá nhân'}</h2>

      <CNav variant="tabs">
        <CNavItem>
          <CNavLink
            active={activeTab === 'profile'}
            style={{ cursor: 'pointer' }}
            onClick={() => setActiveTab('profile')}
          >
            {t('profile.profile') || 'Thông tin'}
          </CNavLink>
        </CNavItem>
        <CNavItem>
          <CNavLink
            active={activeTab === 'password'}
            style={{ cursor: 'pointer' }}
            onClick={() => setActiveTab('password')}
          >
            {t('profile.changePassword') || 'Đổi mật khẩu'}
          </CNavLink>
        </CNavItem>
      </CNav>
      <CTabContent>
        <CTabPane visible={activeTab === 'profile'}>
          <CCard className="mt-3">
            <CCardHeader>
              <h5>{t('profile.profileInformation') || 'Thông tin cá nhân'}</h5>
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
                    <CIcon icon={cilEnvelopeClosed} />
                  </CInputGroupText>
                  <CFormInput
                    type="email"
                    name="email"
                    placeholder={t('common.email') || 'Email'}
                    value={formData.email}
                    disabled
                    readOnly
                  />
                </CInputGroup>

                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilUser} />
                  </CInputGroupText>
                  <CFormInput
                    type="text"
                    name="firstName"
                    placeholder={t('profile.firstName') || 'Họ'}
                    value={formData.firstName}
                    onChange={handleChange}
                    required
                  />
                </CInputGroup>

                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilUser} />
                  </CInputGroupText>
                  <CFormInput
                    type="text"
                    name="lastName"
                    placeholder={t('profile.lastName') || 'Tên'}
                    value={formData.lastName}
                    onChange={handleChange}
                    required
                  />
                </CInputGroup>

                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilBuilding} />
                  </CInputGroupText>
                  <OrganizationSelect
                    value={formData.organizationId || undefined}
                    onChange={handleOrganizationChange}
                    placeholder={t('profile.organization') || 'Tổ chức/Trường'}
                  />
                </CInputGroup>

                <CInputGroup className="mb-3">
                  <CInputGroupText>
                    <CIcon icon={cilPhone} />
                  </CInputGroupText>
                  <CFormInput
                    type="tel"
                    name="phone"
                    placeholder={t('profile.phone') || 'Số điện thoại'}
                    value={formData.phone}
                    onChange={handleChange}
                  />
                </CInputGroup>

                <div className="mb-3">
                  <strong>{t('profile.status') || 'Trạng thái'}:</strong>{' '}
                  {user.emailVerified ? (
                    <span className="text-success">
                      <CIcon icon={cilCheckCircle} /> {t('profile.emailVerified') || 'Đã xác thực email'}
                    </span>
                  ) : (
                    <span className="text-danger">
                      <CIcon icon={cilXCircle} /> {t('profile.emailNotVerified') || 'Chưa xác thực email'}
                    </span>
                  )}
                </div>

                <div className="mb-3">
                  <strong>{t('profile.roles') || 'Vai trò'}:</strong>{' '}
                  {user.roles?.join(', ') || t('profile.noRoles') || 'Không có vai trò'}
                </div>

                <div className="d-grid">
                  <CButton color="primary" type="submit" disabled={saving}>
                    {saving
                      ? t('common.saving') || 'Đang lưu...'
                      : t('common.save') || 'Lưu thay đổi'}
                  </CButton>
                </div>
              </CForm>
            </CCardBody>
          </CCard>
        </CTabPane>
        <CTabPane visible={activeTab === 'password'}>
          <div className="mt-3">
            <ChangePasswordForm />
          </div>
        </CTabPane>
      </CTabContent>
    </div>
  )
}

export default ProfilePage
