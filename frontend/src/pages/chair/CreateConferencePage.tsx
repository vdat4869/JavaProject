import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CFormSelect,
  CButton,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import {
  conferenceService,
  ConferenceCreateRequest,
  Track,
  Deadline,
  Topic,
} from '../../services/conference.service'

/**
 * CreateConferencePage - Trang tạo conference mới
 *
 * Features:
 * - Form tạo conference với đầy đủ thông tin
 * - Tracks và deadlines có thể thêm sau khi tạo
 */
const CreateConferencePage: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [formData, setFormData] = useState<ConferenceCreateRequest>({
    name: '',
    acronym: '',
    description: '',
    reviewMode: 'DOUBLE_BLIND',
    topics: [],
    tracks: [],
    deadlines: [],
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!formData.name.trim()) {
      setError(t('conference.nameRequired') || 'Tên hội nghị là bắt buộc')
      return
    }

    try {
      setLoading(true)
      const created = await conferenceService.createConference(formData)
      navigate(`/app/chair/conference/${created.id}/config`)
    } catch (err: any) {
      setError(err.response?.data?.message || t('conference.createFailed') || 'Không thể tạo hội nghị')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h2 className="mb-4">{t('conference.createConference') || 'Tạo hội nghị mới'}</h2>

      <CCard>
        <CCardHeader>
          <h5>{t('conference.basicInformation') || 'Thông tin cơ bản'}</h5>
        </CCardHeader>
        <CCardBody>
          <CForm onSubmit={handleSubmit}>
            {error && (
              <CAlert color="danger" className="mb-3">
                {error}
              </CAlert>
            )}

            <div className="mb-3">
              <CFormLabel>
                {t('conference.name') || 'Tên hội nghị'} <span className="text-danger">*</span>
              </CFormLabel>
              <CFormInput
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder={t('conference.namePlaceholder') || 'Nhập tên hội nghị'}
                required
              />
            </div>

            <div className="mb-3">
              <CFormLabel>{t('conference.acronym') || 'Tên viết tắt'}</CFormLabel>
              <CFormInput
                type="text"
                name="acronym"
                value={formData.acronym}
                onChange={handleChange}
                placeholder={t('conference.acronymPlaceholder') || 'VD: ICML, NeurIPS'}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>{t('conference.description') || 'Mô tả'}</CFormLabel>
              <CFormTextarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows={5}
                placeholder={t('conference.descriptionPlaceholder') || 'Mô tả về hội nghị...'}
              />
            </div>

            <div className="mb-3">
              <CFormLabel>{t('conference.reviewMode') || 'Chế độ review'}</CFormLabel>
              <CFormSelect name="reviewMode" value={formData.reviewMode} onChange={handleChange}>
                <option value="SINGLE_BLIND">{t('conference.singleBlind') || 'Single Blind'}</option>
                <option value="DOUBLE_BLIND">{t('conference.doubleBlind') || 'Double Blind'}</option>
              </CFormSelect>
              <small className="text-muted">
                {t('conference.reviewModeHint') ||
                  'Single Blind: Reviewers biết tác giả. Double Blind: Reviewers không biết tác giả.'}
              </small>
            </div>

            <div className="d-flex gap-2">
              <CButton type="submit" color="primary" disabled={loading}>
                {loading ? (
                  <>
                    <CSpinner size="sm" className="me-2" />
                    {t('common.creating') || 'Đang tạo...'}
                  </>
                ) : (
                  t('conference.create') || 'Tạo hội nghị'
                )}
              </CButton>
              <CButton
                type="button"
                color="secondary"
                onClick={() => navigate('/app/chair/conferences')}
                disabled={loading}
              >
                {t('common.cancel') || 'Hủy'}
              </CButton>
            </div>

            <div className="mt-3">
              <small className="text-muted">
                {t('conference.createHint') ||
                  'Lưu ý: Bạn có thể thêm tracks, deadlines và cấu hình CFP sau khi tạo hội nghị.'}
              </small>
            </div>
          </CForm>
        </CCardBody>
      </CCard>
    </div>
  )
}

export default CreateConferencePage
