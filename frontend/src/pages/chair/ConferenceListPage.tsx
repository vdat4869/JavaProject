import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CButton,
  CSpinner,
  CAlert,
  CBadge,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilPlus, cilPencil, cilTrash, cilCheckCircle, cilXCircle, cilSpeedometer, cilPeople, cilFile, cilCloudUpload } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'
import { useAuth } from '../../context/AuthContext'

/**
 * ConferenceListPage - Trang danh sách conferences của chair
 *
 * Features:
 * - Hiển thị danh sách conferences của chair
 * - Tạo conference mới
 * - Xem và chỉnh sửa conference
 * - Xóa conference
 */
const ConferenceListPage: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')
  const [conferences, setConferences] = useState<ConferenceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deletingId, setDeletingId] = useState<number | null>(null)

  useEffect(() => {
    loadConferences()
  }, [])

  const loadConferences = async () => {
    try {
      setLoading(true)
      setError('')
      const data = await conferenceService.getMyConferences()
      setConferences(data)
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi tải danh sách hội nghị')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm(t('conference.confirmDelete') || 'Bạn có chắc chắn muốn xóa hội nghị này?')) {
      return
    }

    try {
      setDeletingId(id)
      await conferenceService.deleteConference(id)
      await loadConferences()
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi xóa hội nghị')
    } finally {
      setDeletingId(null)
    }
  }

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString('vi-VN')
    } catch {
      return dateString
    }
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>{t('conference.myConferences') || 'Hội nghị của tôi'}</h2>
        {isAdmin && (
          <CButton color="primary" onClick={() => navigate('/app/chair/conferences/new')}>
            <CIcon icon={cilPlus} /> {t('conference.createNew') || 'Tạo hội nghị mới'}
          </CButton>
        )}
      </div>

      {error && (
        <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
          {error}
        </CAlert>
      )}

      <CCard>
        <CCardHeader>
          <h5>{t('conference.conferenceList') || 'Danh sách hội nghị'}</h5>
        </CCardHeader>
        <CCardBody>
          {loading ? (
            <div className="text-center py-5">
              <CSpinner color="primary" />
            </div>
          ) : conferences.length === 0 ? (
            <div className="text-center py-5">
              <p className="text-muted">{t('conference.noConferences') || 'Chưa có hội nghị nào'}</p>
              {isAdmin && (
                <CButton color="primary" onClick={() => navigate('/app/chair/conferences/new')}>
                  <CIcon icon={cilPlus} /> {t('conference.createFirst') || 'Tạo hội nghị đầu tiên'}
                </CButton>
              )}
            </div>
          ) : (
            <CTable hover responsive>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>{t('conference.name') || 'Tên'}</CTableHeaderCell>
                  <CTableHeaderCell>{t('conference.acronym') || 'Viết tắt'}</CTableHeaderCell>
                  <CTableHeaderCell>{t('conference.status') || 'Trạng thái'}</CTableHeaderCell>
                  <CTableHeaderCell>{t('conference.cfpStatus') || 'CFP'}</CTableHeaderCell>
                  <CTableHeaderCell>{t('conference.createdAt') || 'Ngày tạo'}</CTableHeaderCell>
                  <CTableHeaderCell>{t('common.actions') || 'Thao tác'}</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {conferences.map((conference) => (
                  <CTableRow key={conference.id}>
                    <CTableDataCell>
                      <strong>{conference.name}</strong>
                    </CTableDataCell>
                    <CTableDataCell>{conference.acronym || '-'}</CTableDataCell>
                    <CTableDataCell>
                      {conference.published ? (
                        <CBadge color="success">
                          <CIcon icon={cilCheckCircle} /> {t('conference.published') || 'Đã publish'}
                        </CBadge>
                      ) : (
                        <CBadge color="secondary">
                          <CIcon icon={cilXCircle} /> {t('conference.draft') || 'Bản nháp'}
                        </CBadge>
                      )}
                    </CTableDataCell>
                    <CTableDataCell>
                      {conference.cfp?.open ? (
                        <CBadge color="success">{t('conference.cfpOpen') || 'Mở'}</CBadge>
                      ) : conference.cfp ? (
                        <CBadge color="danger">{t('conference.cfpClosed') || 'Đóng'}</CBadge>
                      ) : (
                        <CBadge color="warning">{t('conference.cfpNotSet') || 'Chưa thiết lập'}</CBadge>
                      )}
                    </CTableDataCell>
                    <CTableDataCell>{formatDate(conference.createdAt)}</CTableDataCell>
                    <CTableDataCell>
                      <div className="d-flex gap-2">
                        <CButton
                          color="info"
                          size="sm"
                          variant="outline"
                          title={t('conference.assignmentsDashboard')}
                          onClick={() => navigate(`/app/chair/assignments?conferenceId=${conference.id}`)}
                        >
                          <CIcon icon={cilSpeedometer} />
                        </CButton>
                        <CButton
                          color="info"
                          size="sm"
                          variant="outline"
                          title={t('conference.managePC')}
                          onClick={() => navigate(`/app/chair/pc?conferenceId=${conference.id}`)}
                        >
                          <CIcon icon={cilPeople} />
                        </CButton>
                        <CButton
                          color="info"
                          size="sm"
                          variant="outline"
                          title={t('conference.manageSubmissions')}
                          onClick={() => navigate(`/app/chair/submissions?conferenceId=${conference.id}`)}
                        >
                          <CIcon icon={cilFile} />
                        </CButton>
                        <CButton
                          color="success"
                          size="sm"
                          variant="outline"
                          title={t('conference.decisionBoard')}
                          onClick={() => navigate(`/app/chair/decisions?conferenceId=${conference.id}`)}
                        >
                          <CIcon icon={cilCheckCircle} />
                        </CButton>
                        <CButton
                          color="primary"
                          size="sm"
                          variant="outline"
                          title={t('conference.cameraReadyBoard')}
                          onClick={() => navigate(`/app/chair/camera-ready?conferenceId=${conference.id}`)}
                        >
                          <CIcon icon={cilCloudUpload} />
                        </CButton>

                        <CButton
                          color="primary"
                          size="sm"
                          className="me-2"
                          onClick={() => navigate(`/app/chair/conference/${conference.id}/config`)}
                        >
                          <CIcon icon={cilPencil} /> {t('common.edit') || 'Sửa'}
                        </CButton>
                        {isAdmin && (
                          <CButton
                            color="danger"
                            size="sm"
                            onClick={() => handleDelete(conference.id)}
                            disabled={deletingId === conference.id}
                          >
                            {deletingId === conference.id ? (
                              <CSpinner size="sm" />
                            ) : (
                              <>
                                <CIcon icon={cilTrash} /> {t('common.delete') || 'Xóa'}
                              </>
                            )}
                          </CButton>
                        )}
                      </div>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>
    </div>
  )
}

export default ConferenceListPage
