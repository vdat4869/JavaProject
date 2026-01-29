import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
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
  CNav,
  CNavItem,
  CNavLink,
  CTabContent,
  CTabPane,
  CBadge,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilCheckCircle, cilXCircle, cilTrash } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import {
  conferenceService,
  ConferenceResponse,
  CFPResponse,
  ConferenceUpdateRequest,
  CFPRequest,
} from '../../services/conference.service'
import TrackEditor from '../../components/conference/TrackEditor'
import DeadlineEditor from '../../components/conference/DeadlineEditor'

/**
 * ConferenceConfig - Trang cấu hình conference
 *
 * Features:
 * - Cập nhật thông tin conference
 * - Cấu hình CFP (call for papers, submission guidelines)
 * - Publish/Close CFP
 * - Chỉ CHAIR mới có quyền
 */
const ConferenceConfig: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [conference, setConference] = useState<ConferenceResponse | null>(null)
  const [cfp, setCfp] = useState<CFPResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [publishing, setPublishing] = useState(false)
  const [closing, setClosing] = useState(false)
  const [activeTab, setActiveTab] = useState('basic')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (id) {
      loadData()
    }
  }, [id])

  const loadData = async () => {
    try {
      setLoading(true)
      setError('')
      const conferenceId = parseInt(id!)
      const [confData, cfpData] = await Promise.all([
        conferenceService.getConference(conferenceId),
        conferenceService.getCFP(conferenceId).catch(() => null), // CFP might not exist yet
      ])
      setConference(confData)
      setCfp(cfpData)
    } catch (err: any) {
      console.error('Error loading conference data:', err)
      setError(err.response?.data?.message || t('conference.loadError'))
    } finally {
      setLoading(false)
    }
  }

  const handleUpdateConference = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (!conference) return

    try {
      setSaving(true)
      const updateData: ConferenceUpdateRequest = {
        name: conference.name,
        acronym: conference.acronym,
        description: conference.description,
        reviewMode: conference.reviewMode,
        tracks: conference.tracks,
        deadlines: conference.deadlines,
      }
      const updated = await conferenceService.updateConference(parseInt(id!), updateData)
      setConference(updated)
      setSuccess(t('conference.updateSuccess'))
    } catch (err: any) {
      setError(err.response?.data?.message || t('conference.updateFailed'))
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateCFP = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (!conference) return

    try {
      setSaving(true)
      const cfpData: CFPRequest = {
        conferenceId: conference.id,
        callForPapers: cfp?.callForPapers || '',
        submissionGuidelines: cfp?.submissionGuidelines || '',
        topicIds: conference.topics?.map((t) => t.id!).filter((id) => id !== undefined),
      }
      const updated = await conferenceService.createOrUpdateCFP(cfpData)
      setCfp(updated)
      setSuccess(t('conference.cfpUpdateSuccess'))
    } catch (err: any) {
      setError(err.response?.data?.message || t('conference.cfpUpdateFailed'))
    } finally {
      setSaving(false)
    }
  }

  const handlePublishCFP = async () => {
    if (!conference) return
    if (!window.confirm(t('conference.confirmPublishCFP'))) {
      return
    }

    try {
      setPublishing(true)
      setError('')
      const updated = await conferenceService.publishCFP(conference.id)
      setCfp(updated)
      setSuccess(t('conference.cfpPublished'))
      await loadData() // Reload to get updated conference status
    } catch (err: any) {
      setError(err.response?.data?.message || t('conference.cfpPublishFailed'))
    } finally {
      setPublishing(false)
    }
  }

  const handleCloseCFP = async () => {
    if (!conference) return
    if (!window.confirm(t('conference.confirmCloseCFP'))) {
      return
    }

    try {
      setClosing(true)
      setError('')
      const updated = await conferenceService.closeCFP(conference.id)
      setCfp(updated)
      setSuccess(t('conference.cfpCloseSuccess'))
      await loadData() // Reload to get updated conference status
    } catch (err: any) {
      setError(err.response?.data?.message || t('conference.cfpCloseFailed'))
    } finally {
      setClosing(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (error && !conference) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{error}</CAlert>
          <CButton color="primary" onClick={() => loadData()}>
            {t('common.refresh') || 'Làm mới'}
          </CButton>
        </CCardBody>
      </CCard>
    )
  }

  if (!conference) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{t('conference.notFound') || 'Không tìm thấy hội nghị'}</CAlert>
          <CButton color="secondary" onClick={() => navigate('/app/chair/conferences')}>
            {t('common.back')}
          </CButton>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>
          {t('conference.config') || 'Cấu hình hội nghị'}: {conference.name}
        </h2>
        <div className="d-flex gap-2">
          {!conference.published && (
            <CButton
              color="success"
              onClick={async () => {
                if (window.confirm(t('conference.confirmPublish') || 'Công khai hội nghị này?')) {
                  try {
                    setSaving(true)
                    const updated = await conferenceService.updateConference(conference.id, {
                      published: true,
                    })
                    setConference(updated)
                    setSuccess(t('conference.publishSuccess'))
                  } catch (err: any) {
                    setError(err.response?.data?.message || t('conference.publishFailed'))
                  } finally {
                    setSaving(false)
                  }
                }
              }}
              disabled={saving}
            >
              <CIcon icon={cilCheckCircle} className="me-2" />
              {t('conference.publish') || 'Công bố'}
            </CButton>
          )}
          <CButton
            color="danger"
            onClick={async () => {
              if (
                window.confirm(
                  t('conference.confirmDelete') ||
                  'Bạn có chắc chắn muốn xóa hội nghị này? Hành động này không thể hoàn tác!',
                )
              ) {
                try {
                  setSaving(true)
                  await conferenceService.deleteConference(conference.id)
                  navigate('/app/chair/conferences')
                } catch (err: any) {
                  setError(err.response?.data?.message || t('conference.deleteFailed') || 'Không thể xóa hội nghị')
                } finally {
                  setSaving(false)
                }
              }
            }}
            disabled={saving}
          >
            <CIcon icon={cilTrash} className="me-2" />
            {t('common.delete')}
          </CButton>
          <CButton color="secondary" onClick={() => navigate('/app/chair/conferences')}>
            {t('common.back')}
          </CButton>
        </div>
      </div>

      {conference.published && (
        <CAlert color="info" className="mb-3">
          <CIcon icon={cilCheckCircle} /> {t('conference.published') || 'Hội nghị đã được publish'}
        </CAlert>
      )}

      <CCard>
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h5>{t('conference.conferenceConfiguration') || 'Cấu hình hội nghị'}</h5>
            {cfp && (
              <div>
                <CBadge color={cfp.open ? 'success' : 'danger'} className="me-2">
                  {cfp.open
                    ? t('conference.cfpOpen') || 'CFP Mở'
                    : t('conference.cfpClosed') || 'CFP Đóng'}
                </CBadge>
              </div>
            )}
          </div>
        </CCardHeader>
        <CCardBody>
          {error && (
            <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
              {error}
            </CAlert>
          )}
          {success && (
            <CAlert color="success" className="mb-3" onClose={() => setSuccess('')} dismissible>
              {success}
            </CAlert>
          )}

          <CNav variant="tabs" className="mb-3">
            <CNavItem>
              <CNavLink
                active={activeTab === 'basic'}
                onClick={() => setActiveTab('basic')}
                style={{ cursor: 'pointer' }}
              >
                {t('conference.basicInfo') || 'Thông tin cơ bản'}
              </CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink
                active={activeTab === 'cfp'}
                onClick={() => setActiveTab('cfp')}
                style={{ cursor: 'pointer' }}
              >
                {t('conference.cfp') || 'CFP'}
                {cfp && (
                  <CBadge
                    color={cfp.open ? 'success' : 'danger'}
                    className="ms-2"
                    style={{ fontSize: '0.7rem' }}
                  >
                    {cfp.open ? 'Mở' : 'Đóng'}
                  </CBadge>
                )}
              </CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink
                active={activeTab === 'tracks'}
                onClick={() => setActiveTab('tracks')}
                style={{ cursor: 'pointer' }}
              >
                {t('conference.tracks') || 'Tracks'}
              </CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink
                active={activeTab === 'deadlines'}
                onClick={() => setActiveTab('deadlines')}
                style={{ cursor: 'pointer' }}
              >
                {t('conference.deadlines') || 'Deadlines'}
              </CNavLink>
            </CNavItem>
          </CNav>
          <CTabContent>
            <CTabPane visible={activeTab === 'basic'}>
              <CForm onSubmit={handleUpdateConference}>
                <div className="mb-3">
                  <CFormLabel>
                    {t('conference.name') || 'Tên hội nghị'} <span className="text-danger">*</span>
                  </CFormLabel>
                  <CFormInput
                    type="text"
                    value={conference.name}
                    onChange={(e) => setConference({ ...conference, name: e.target.value })}
                    required
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>{t('conference.acronym') || 'Tên viết tắt'}</CFormLabel>
                  <CFormInput
                    type="text"
                    value={conference.acronym || ''}
                    onChange={(e) => setConference({ ...conference, acronym: e.target.value })}
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>{t('conference.description') || 'Mô tả'}</CFormLabel>
                  <CFormTextarea
                    value={conference.description || ''}
                    onChange={(e) => setConference({ ...conference, description: e.target.value })}
                    rows={5}
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>{t('conference.reviewMode') || 'Chế độ review'}</CFormLabel>
                  <CFormSelect
                    value={conference.reviewMode || 'DOUBLE_BLIND'}
                    onChange={(e) => setConference({ ...conference, reviewMode: e.target.value })}
                  >
                    <option value="SINGLE_BLIND">
                      {t('conference.singleBlind') || 'Single Blind'}
                    </option>
                    <option value="DOUBLE_BLIND">
                      {t('conference.doubleBlind') || 'Double Blind'}
                    </option>
                  </CFormSelect>
                </div>
                <CButton type="submit" color="primary" disabled={saving}>
                  {saving ? (
                    <>
                      <CSpinner size="sm" className="me-2" />
                      {t('common.saving') || 'Đang lưu...'}
                    </>
                  ) : (
                    t('common.save') || 'Lưu'
                  )}
                </CButton>
              </CForm>
            </CTabPane>
            <CTabPane visible={activeTab === 'cfp'}>
              <CForm onSubmit={handleUpdateCFP}>
                <div className="mb-3">
                  <CFormLabel>{t('conference.cfpDescription') || 'Mô tả CFP (Call for Papers)'}</CFormLabel>
                  <CFormTextarea
                    value={cfp?.callForPapers || ''}
                    onChange={(e) => setCfp(cfp ? { ...cfp, callForPapers: e.target.value } : { callForPapers: e.target.value } as any)}
                    rows={8}
                    placeholder={t('conference.cfpDescriptionPlaceholder') || 'Nhập mô tả CFP...'}
                  />
                </div>
                <div className="mb-3">
                  <CFormLabel>
                    {t('conference.submissionGuidelines') || 'Hướng dẫn nộp bài'}
                  </CFormLabel>
                  <CFormTextarea
                    value={cfp?.submissionGuidelines || ''}
                    onChange={(e) => setCfp(cfp ? { ...cfp, submissionGuidelines: e.target.value } : { submissionGuidelines: e.target.value } as any)}
                    rows={5}
                    placeholder={t('conference.submissionGuidelinesPlaceholder') || 'Nhập hướng dẫn nộp bài...'}
                  />
                </div>
                <div className="d-flex gap-2 mb-3">
                  <CButton type="submit" color="primary" disabled={saving}>
                    {saving ? (
                      <>
                        <CSpinner size="sm" className="me-2" />
                        {t('common.saving') || 'Đang lưu...'}
                      </>
                    ) : (
                      t('conference.saveCFP') || 'Lưu CFP'
                    )}
                  </CButton>
                  {cfp && cfp.id && (
                    <>
                      {cfp.open ? (
                        <CButton
                          type="button"
                          color="warning"
                          onClick={handleCloseCFP}
                          disabled={closing}
                        >
                          {closing ? (
                            <>
                              <CSpinner size="sm" className="me-2" />
                              {t('common.processing') || 'Đang xử lý...'}
                            </>
                          ) : (
                            <>
                              <CIcon icon={cilXCircle} className="me-2" />
                              {t('conference.closeCFP') || 'Đóng CFP'}
                            </>
                          )}
                        </CButton>
                      ) : (
                        <CButton
                          type="button"
                          color="success"
                          onClick={handlePublishCFP}
                          disabled={publishing}
                        >
                          {publishing ? (
                            <>
                              <CSpinner size="sm" className="me-2" />
                              {t('common.processing') || 'Đang xử lý...'}
                            </>
                          ) : (
                            <>
                              <CIcon icon={cilCheckCircle} className="me-2" />
                              {t('conference.publishCFP') || 'Publish CFP'}
                            </>
                          )}
                        </CButton>
                      )}
                    </>
                  )}
                </div>
                {!cfp && (
                  <CAlert color="info">
                    {t('conference.cfpNotCreated') ||
                      'CFP chưa được tạo. Lưu CFP để tạo mới, sau đó có thể publish.'}
                  </CAlert>
                )}
              </CForm>
            </CTabPane>
            <CTabPane visible={activeTab === 'tracks'}>
              <TrackEditor
                tracks={conference.tracks || []}
                onChange={(tracks) => {
                  setConference({ ...conference, tracks })
                  setSuccess('') // Clear success message
                }}
              />
              <div className="mt-3">
                <CButton
                  color="primary"
                  onClick={async () => {
                    try {
                      setSaving(true)
                      const updateData: ConferenceUpdateRequest = {
                        tracks: conference.tracks,
                      }
                      await conferenceService.updateConference(parseInt(id!), updateData)
                      setSuccess(t('conference.tracksUpdated'))
                    } catch (err: any) {
                      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi cập nhật tracks')
                    } finally {
                      setSaving(false)
                    }
                  }}
                  disabled={saving}
                >
                  {saving ? (
                    <>
                      <CSpinner size="sm" className="me-2" />
                      {t('common.saving') || 'Đang lưu...'}
                    </>
                  ) : (
                    t('common.saveTracks') || 'Lưu Tracks'
                  )}
                </CButton>
              </div>
            </CTabPane>
            <CTabPane visible={activeTab === 'deadlines'}>
              <DeadlineEditor
                deadlines={conference.deadlines || []}
                onChange={(deadlines) => {
                  setConference({ ...conference, deadlines })
                  setSuccess('') // Clear success message
                }}
              />
              <div className="mt-3">
                <CButton
                  color="primary"
                  onClick={async () => {
                    try {
                      setSaving(true)
                      const updateData: ConferenceUpdateRequest = {
                        deadlines: conference.deadlines,
                      }
                      await conferenceService.updateConference(parseInt(id!), updateData)
                      setSuccess(t('conference.deadlinesUpdated'))
                    } catch (err: any) {
                      setError(
                        err.response?.data?.message || t('common.error') || 'Lỗi khi cập nhật deadlines',
                      )
                    } finally {
                      setSaving(false)
                    }
                  }}
                  disabled={saving}
                >
                  {saving ? (
                    <>
                      <CSpinner size="sm" className="me-2" />
                      {t('common.saving') || 'Đang lưu...'}
                    </>
                  ) : (
                    t('common.saveDeadlines') || 'Lưu Deadlines'
                  )}
                </CButton>
              </div>
            </CTabPane>
          </CTabContent>
        </CCardBody>
      </CCard>
    </div>
  )
}

export default ConferenceConfig
