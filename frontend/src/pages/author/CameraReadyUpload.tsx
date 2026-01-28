import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormLabel,
  CButton,
  CAlert,
  CSpinner,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
  CBadge,
  CFormCheck,
} from '@coreui/react'
import {
  cameraReadyService,
  CameraReadySubmission,
  CameraReadyVersion,
} from '../../services/camera-ready.service'
import { submissionService } from '../../services/submission.service'

/**
 * CameraReadyUpload - Trang upload camera-ready cho AUTHOR (API v1)
 */
const CameraReadyUpload: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const paperId = id // String UUID

  const [submission, setSubmission] = useState<CameraReadySubmission | null>(null)
  const [versions, setVersions] = useState<CameraReadyVersion[]>([])
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [copyrightAccepted, setCopyrightAccepted] = useState(false)

  useEffect(() => {
    if (paperId) {
      loadInitialData()
    }
  }, [paperId])

  const loadInitialData = async () => {
    try {
      setLoading(true)
      // 1. Lấy thông tin submission gốc để có conferenceId
      const legacySub = await submissionService.getSubmission(parseInt(paperId!))
      const conferenceId = legacySub.conferenceId.toString()

      // 2. Lấy thông tin camera-ready submission
      const data = await cameraReadyService.getSubmission(conferenceId, paperId!)
      if (data) {
        setSubmission(data)
        setCopyrightAccepted(data.copyrightConfirmed)
      } else {
        throw new Error('Không tìm thấy dữ liệu Camera-Ready')
      }

      // 3. Lấy danh sách versions
      const versionsData = await cameraReadyService.listVersions(conferenceId, paperId!)
      setVersions(versionsData)
    } catch (error: any) {
      console.error('Error loading camera-ready data:', error)
      if (error.response?.status === 404) {
        setError('Chức năng nộp bản thảo cuối (Camera-ready) chưa được mở hoặc bài báo chưa được chấp nhận.')
      } else {
        setError('Không thể tải dữ liệu camera-ready. Vui lòng thử lại sau.')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0]
      if (selectedFile.type !== 'application/pdf') {
        setError('Chỉ chấp nhận file PDF')
        return
      }
      if (selectedFile.size > 20 * 1024 * 1024) {
        setError('File không được vượt quá 20MB')
        return
      }
      setFile(selectedFile)
      setError('')
    }
  }

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!file || !submission) return

    try {
      setUploading(true)
      setError('')
      await cameraReadyService.uploadVersion(submission.conferenceId, submission.paperId, file)
      setSuccess('Tải lên phiên bản mới thành công')
      setFile(null)
      // Reset input
      const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
      if (fileInput) fileInput.value = ''

      await loadInitialData()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể upload file')
    } finally {
      setUploading(false)
    }
  }

  const handleConfirmCopyright = async () => {
    if (!submission || !copyrightAccepted) return

    try {
      setConfirming(true)
      setError('')
      await cameraReadyService.confirmCopyright(
        submission.conferenceId,
        submission.paperId,
        true
      )
      setSuccess('Xác nhận bản quyền thành công')
      await loadInitialData()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể xác nhận bản quyền')
    } finally {
      setConfirming(false)
    }
  }

  const handleDownload = async (versionId: string, filename: string) => {
    if (!submission) return
    try {
      const blob = await cameraReadyService.downloadVersion(
        submission.conferenceId,
        submission.paperId,
        versionId
      )
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (error) {
      setError('Không thể tải xuống file')
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!submission) {
    return (
      <div className="container-lg mt-4">
        <CAlert color="danger">
          {error || 'Không tìm thấy thông tin camera-ready cho bài báo này.'}
        </CAlert>
        <CButton color="secondary" onClick={() => navigate(-1)}>
          Quay lại
        </CButton>
      </div>
    )
  }

  return (
    <div className="container-lg">
      <CCard className="mb-4">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Bản thảo cuối (Camera-ready): {submission.paperTitle}</h4>
            <CButton color="secondary" size="sm" onClick={() => navigate(-1)}>
              Quay lại
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}
          {success && <CAlert color="success" dismissible onClose={() => setSuccess('')}>{success}</CAlert>}

          <div className="row">
            <div className="col-md-6">
              <h5>Thông tin chung</h5>
              <div className="mb-3">
                <p><strong>Trạng thái:</strong> <CBadge color={submission.status === 'APPROVED' ? 'success' : 'info'}>{submission.status}</CBadge></p>
                <p><strong>Lĩnh vực:</strong> {submission.trackName}</p>
                <p><strong>Thời hạn nộp bài:</strong> <span className="text-danger">{new Date(submission.deadline).toLocaleString('vi-VN')}</span></p>
                <p><strong>Xác nhận bản quyền:</strong> {submission.copyrightConfirmed ? <CBadge color="success">Đã xác nhận</CBadge> : <CBadge color="warning">Chưa xác nhận</CBadge>}</p>
              </div>
            </div>

            <div className="col-md-6 border-start">
              <h5>Tải lên phiên bản mới</h5>
              {!submission.canUpload ? (
                <CAlert color="warning">Quy trình nộp bản thảo cuối đã đóng hoặc bài báo chưa sẵn sàng.</CAlert>
              ) : (
                <CForm onSubmit={handleUpload}>
                  <div className="mb-3">
                    <CFormLabel>Chọn file PDF (Tối đa 20MB)</CFormLabel>
                    <CFormInput type="file" accept=".pdf" onChange={handleFileChange} required />
                  </div>
                  <CButton type="submit" color="primary" disabled={uploading || !file}>
                    {uploading ? <CSpinner size="sm" /> : 'Tải lên phiên bản mới'}
                  </CButton>
                </CForm>
              )}
            </div>
          </div>

          <hr className="my-4" />

          <h5>Danh sách các phiên bản đã nộp</h5>
          {versions.length === 0 ? (
            <p className="text-muted">Chưa có phiên bản nào được nộp.</p>
          ) : (
            <CTable hover responsive align="middle">
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>#</CTableHeaderCell>
                  <CTableHeaderCell>Tên file</CTableHeaderCell>
                  <CTableHeaderCell>Kích thước</CTableHeaderCell>
                  <CTableHeaderCell>Thời gian nộp</CTableHeaderCell>
                  <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                  <CTableHeaderCell>Thao tác</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {versions.map((v) => (
                  <CTableRow key={v.id} active={v.isCurrent}>
                    <CTableDataCell>{v.versionNumber}</CTableDataCell>
                    <CTableDataCell>{v.originalFilename}</CTableDataCell>
                    <CTableDataCell>{(v.fileSizeBytes / 1024 / 1024).toFixed(2)} MB</CTableDataCell>
                    <CTableDataCell>{new Date(v.uploadedAt).toLocaleString('vi-VN')}</CTableDataCell>
                    <CTableDataCell>
                      {v.isCurrent && <CBadge color="primary" className="me-1">Hiện tại</CBadge>}
                      {v.validationPassed ? <CBadge color="success">Hợp lệ</CBadge> : <CBadge color="danger">Lỗi định dạng</CBadge>}
                    </CTableDataCell>
                    <CTableDataCell>
                      <CButton color="info" size="sm" onClick={() => handleDownload(v.id, v.originalFilename)}>
                        Tải xuống
                      </CButton>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}

          <hr className="my-4" />

          <h5>Xác nhận bản quyền (Copyright)</h5>
          <div className="p-3 bg-light rounded shadow-sm border">
            <p>Tôi xác nhận rằng tôi là tác giả (hoặc được ủy quyền bởi tất cả các tác giả) của bài báo này và đồng ý chuyển giao quyền xuất bản cho nhà tổ chức hội nghị theo quy định.</p>
            <CFormCheck
              id="copyrightCheck"
              label="Tôi đồng ý với các điều khoản bản quyền"
              disabled={submission.copyrightConfirmed || !submission.canConfirmCopyright}
              checked={copyrightAccepted}
              onChange={(e) => setCopyrightAccepted(e.target.checked)}
              className="mb-3"
            />
            {!submission.copyrightConfirmed && (
              <CButton
                color="warning"
                disabled={confirming || !copyrightAccepted || !submission.canConfirmCopyright}
                onClick={handleConfirmCopyright}
              >
                {confirming ? <CSpinner size="sm" /> : 'Gửi xác nhận bản quyền'}
              </CButton>
            )}
            {submission.copyrightConfirmed && (
              <p className="text-success mb-0 small">✓ Bản quyền đã được xác nhận lúc {new Date(submission.copyrightConfirmedAt!).toLocaleString('vi-VN')}</p>
            )}
          </div>
        </CCardBody>
      </CCard>
    </div>
  )
}

export default CameraReadyUpload
