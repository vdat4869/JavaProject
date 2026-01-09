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
} from '@coreui/react'
import { cameraReadyService, CameraReadySubmission } from '../../services/camera-ready.service'
import FormatChecklist from '../../components/camera-ready/FormatChecklist'

/**
 * CameraReadyUpload - Trang upload camera-ready cho AUTHOR
 *
 * Features:
 * - Upload camera-ready PDF
 * - Format checklist
 * - Xem trạng thái format check
 */
const CameraReadyUpload: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const submissionId = id ? parseInt(id) : null
  const [cameraReady, setCameraReady] = useState<CameraReadySubmission | null>(null)
  const [file, setFile] = useState<File | null>(null)
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (submissionId) {
      loadCameraReady()
    }
  }, [submissionId])

  const loadCameraReady = async () => {
    try {
      setLoading(true)
      const data = await cameraReadyService.getBySubmissionId(submissionId!)
      setCameraReady(data)
    } catch (error) {
      console.error('Error loading camera-ready:', error)
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
    if (!file) {
      setError('Vui lòng chọn file')
      return
    }

    try {
      setUploading(true)
      setError('')
      if (cameraReady) {
        await cameraReadyService.update(cameraReady.id, file)
      } else {
        await cameraReadyService.upload({
          submissionId: submissionId!,
          file,
        })
      }
      setSuccess('Upload thành công')
      await loadCameraReady()
      setFile(null)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể upload file')
    } finally {
      setUploading(false)
    }
  }

  if (!submissionId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing submissionId</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <h4>Upload Camera-ready</h4>
        </CCardHeader>
        <CCardBody>
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

          {cameraReady && (
            <div className="mb-3">
              <p>
                <strong>File hiện tại: </strong>
                {cameraReady.fileName}
              </p>
              <p>
                <strong>Trạng thái: </strong>
                {cameraReady.status}
              </p>
              <p>
                <strong>Upload lúc: </strong>
                {new Date(cameraReady.submittedAt).toLocaleString('vi-VN')}
              </p>
            </div>
          )}

          <CForm onSubmit={handleUpload}>
            <div className="mb-3">
              <CFormLabel>Chọn file PDF (tối đa 20MB) *</CFormLabel>
              <CFormInput
                type="file"
                accept=".pdf"
                onChange={handleFileChange}
                required={!cameraReady}
              />
              <small className="text-muted">Chỉ chấp nhận file PDF, tối đa 20MB</small>
            </div>

            <CButton type="submit" color="primary" disabled={uploading || !file}>
              {uploading ? <CSpinner size="sm" /> : cameraReady ? 'Cập nhật' : 'Upload'}
            </CButton>
          </CForm>
        </CCardBody>
      </CCard>

      {cameraReady && (
        <FormatChecklist
          cameraReadyId={cameraReady.id}
          submissionId={submissionId}
          formatChecked={cameraReady.formatChecked}
          formatIssues={cameraReady.formatIssues}
        />
      )}
    </>
  )
}

export default CameraReadyUpload
