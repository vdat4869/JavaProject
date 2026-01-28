import React, { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CAlert,
  CSpinner,
  CRow,
  CCol,
  CListGroup,
  CListGroupItem,
  CBadge,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilCloudDownload, cilFile, cilLibrary, cilCode, cilSettings } from '@coreui/icons'
import { cameraReadyService } from '../../services/camera-ready.service'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'

/**
 * ProceedingsExport - Trang xuất bản kỷ yếu (Proceedings) cho CHAIR (API v1)
 */
const ProceedingsExport: React.FC = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const conferenceId = searchParams.get('conferenceId')

  const [exporting, setExporting] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  // Conference selection states
  const [myConferences, setMyConferences] = useState<ConferenceResponse[]>([])
  const [isSelectingConference, setIsSelectingConference] = useState(false)

  React.useEffect(() => {
    if (!conferenceId) {
      loadMyConferences()
    }
  }, [conferenceId])

  const loadMyConferences = async () => {
    try {
      setLoading(true)
      const data = await conferenceService.getMyConferences()
      if (data.length === 1) {
        // Auto select if only one
        navigate(`?conferenceId=${data[0].id}`, { replace: true })
      } else {
        setMyConferences(data)
        setIsSelectingConference(true)
        setLoading(false)
      }
    } catch (error) {
      console.error('Error loading conferences:', error)
      setLoading(false)
    }
  }

  const handleExport = async (format: 'zip' | 'pdf' | 'json' | 'csv') => {
    if (!conferenceId) {
      setError('Thiếu tham số conferenceId')
      return
    }

    try {
      setExporting(format)
      setError('')
      setSuccess('')

      const blob = await cameraReadyService.exportProceedings(conferenceId, format)

      // Download file
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const extension = format === 'zip' ? 'zip' : format === 'pdf' ? 'pdf' : format === 'json' ? 'json' : 'csv'
      link.setAttribute('download', `proceedings-${conferenceId}.${extension}`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)

      setSuccess(`Xuất bản định dạng ${format.toUpperCase()} thành công!`)
    } catch (err: any) {
      setError(err.response?.data?.message || `Không thể xuất bản định dạng ${format.toUpperCase()}`)
    } finally {
      setExporting(null)
    }
  }

  if (!conferenceId) {
    if (loading) {
      return (
        <div className="d-flex justify-content-center p-5">
          <CSpinner color="primary" />
        </div>
      )
    }

    if (isSelectingConference) {
      return (
        <CCard className="mx-auto" style={{ maxWidth: '800px' }}>
          <CCardHeader>
            <h4>Chọn hội nghị để xuất bản kỷ yếu</h4>
          </CCardHeader>
          <CCardBody>
            {myConferences.length === 0 ? (
              <CAlert color="warning">Bạn chưa được gán vào hội nghị nào.</CAlert>
            ) : (
              <CListGroup>
                {myConferences.map(conf => (
                  <CListGroupItem
                    key={conf.id}
                    onClick={() => navigate(`?conferenceId=${conf.id}`)}
                    className="d-flex justify-content-between align-items-center list-group-item-action"
                    style={{ cursor: 'pointer' }}
                  >
                    <span>{conf.name}</span>
                    <CBadge color="primary" shape="rounded-pill">ID: {conf.id}</CBadge>
                  </CListGroupItem>
                ))}
              </CListGroup>
            )}
          </CCardBody>
        </CCard>
      )
    }

    return (
      <CAlert color="danger">
        Thiếu tham số conferenceId. Vui lòng quay lại từ trang quản lý hội nghị.
      </CAlert>
    )
  }

  return (
    <div className="container-lg">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Xuất bản kỷ yếu (Proceedings)</h3>
        <div>
          <CButton color="secondary" variant="outline" className="me-2" onClick={() => navigate('/app/chair/conferences')}>
            Đổi hội nghị
          </CButton>
          <CButton color="secondary" onClick={() => navigate(-1)}>Quay lại</CButton>
        </div>
      </div>

      <CCard className="mb-4">
        <CCardHeader>
          <CIcon icon={cilSettings} className="me-2" />
          Tùy chọn xuất bản
        </CCardHeader>
        <CCardBody>
          <p className="text-muted mb-4">
            Hệ thống sẽ tổng hợp tất cả các bài báo đã được <strong>PHÊ DUYỆT (APPROVED)</strong> trong giai đoạn Camera-ready để tạo kỷ yếu.
          </p>

          {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
          {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

          <CRow>
            <CCol md={6} lg={3} className="mb-3">
              <CCard className="h-100 text-center p-3 border-primary">
                <div className="mb-3">
                  <CIcon icon={cilLibrary} size="xl" className="text-primary" />
                </div>
                <h5>Tất cả PDF (ZIP)</h5>
                <p className="small text-muted flex-grow-1">Bao gói tất cả các tệp PDF camera-ready thành một bản nén ZIP duy nhất.</p>
                <CButton
                  color="primary"
                  disabled={!!exporting}
                  onClick={() => handleExport('zip')}
                >
                  {exporting === 'zip' ? <CSpinner size="sm" /> : 'Tải ZIP'}
                </CButton>
              </CCard>
            </CCol>

            <CCol md={6} lg={3} className="mb-3">
              <CCard className="h-100 text-center p-3 border-danger">
                <div className="mb-3">
                  <CIcon icon={cilFile} size="xl" className="text-danger" />
                </div>
                <h5>Kỷ yếu (PDF)</h5>
                <p className="small text-muted flex-grow-1">Tạo một tệp PDF duy nhất bao gồm bìa, mục lục và toàn bộ nội dung các bài báo.</p>
                <CButton
                  color="danger"
                  disabled={!!exporting}
                  onClick={() => handleExport('pdf')}
                >
                  {exporting === 'pdf' ? <CSpinner size="sm" /> : 'Tải PDF'}
                </CButton>
              </CCard>
            </CCol>

            <CCol md={6} lg={3} className="mb-3">
              <CCard className="h-100 text-center p-3 border-info">
                <div className="mb-3">
                  <CIcon icon={cilCode} size="xl" className="text-info" />
                </div>
                <h5>Siêu dữ liệu (JSON)</h5>
                <p className="small text-muted flex-grow-1">Dữ liệu cấu trúc chứa tiêu đề, tác giả, tóm tắt bài báo để tích hợp hệ thống khác.</p>
                <CButton
                  color="info"
                  disabled={!!exporting}
                  onClick={() => handleExport('json')}
                >
                  {exporting === 'json' ? <CSpinner size="sm" /> : 'Tải JSON'}
                </CButton>
              </CCard>
            </CCol>

            <CCol md={6} lg={3} className="mb-3">
              <CCard className="h-100 text-center p-3 border-success">
                <div className="mb-3">
                  <CIcon icon={cilCloudDownload} size="xl" className="text-success" />
                </div>
                <h5>Bảng tổng hợp (CSV)</h5>
                <p className="small text-muted flex-grow-1">Danh sách chi tiết bài nộp định dạng bảng Excel để quản lý hành chính.</p>
                <CButton
                  color="success"
                  disabled={!!exporting}
                  onClick={() => handleExport('csv')}
                >
                  {exporting === 'csv' ? <CSpinner size="sm" /> : 'Tải CSV'}
                </CButton>
              </CCard>
            </CCol>
          </CRow>
        </CCardBody>
      </CCard>
    </div>
  )
}

export default ProceedingsExport
