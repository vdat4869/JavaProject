import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormLabel,
  CButton,
  CSpinner,
  CAlert,
  CBadge,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { reviewService, Rebuttal, RebuttalSubmitDTO } from '../../services/review.service'

/**
 * RebuttalFormPage - Trang tạo/chỉnh sửa rebuttal cho authors
 *
 * Features:
 * - Create draft rebuttal
 * - Edit existing rebuttal
 * - Submit rebuttal (finalize)
 */
const RebuttalFormPage: React.FC = () => {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const submissionId = id ? parseInt(id) : null
  const [content, setContent] = useState('')
  const [rebuttal, setRebuttal] = useState<Rebuttal | null>(null)
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [loadingData, setLoadingData] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (submissionId) {
      loadRebuttal()
    }
  }, [submissionId])

  const loadRebuttal = async () => {
    try {
      setLoadingData(true)
      const data = await reviewService.getRebuttal(submissionId!)
      if (data) {
        console.log('Rebuttal data loaded:', data)
        setRebuttal(data)
        setContent(data.content || '')
      } else {
        console.log('No existing rebuttal found for this submission.')
        setRebuttal(null)
      }
    } catch (error: any) {
      if (error.response?.status !== 404) {
        console.error('Error loading rebuttal:', error)
        setError('Không thể tải rebuttal')
      }
    } finally {
      setLoadingData(false)
    }
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!content.trim()) {
      setError('Vui lòng nhập nội dung rebuttal')
      return
    }

    if (!submissionId) {
      setError('Missing submission ID')
      return
    }

    try {
      setLoading(true)
      const submitData: RebuttalSubmitDTO = {
        submissionId,
        content: content.trim(),
      }

      const savedRebuttal = await reviewService.createOrUpdateRebuttal(submitData)
      setRebuttal(savedRebuttal)
      alert('Đã lưu rebuttal thành công')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể lưu rebuttal')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async () => {
    if (!rebuttal) {
      setError('Vui lòng lưu rebuttal trước khi submit')
      return
    }

    if (
      !window.confirm(
        'Bạn có chắc chắn muốn submit rebuttal này? Sau khi submit, bạn không thể chỉnh sửa.',
      )
    ) {
      return
    }

    try {
      setSubmitting(true)
      await reviewService.submitRebuttal(rebuttal.id)
      alert('Đã submit rebuttal thành công')
      navigate(`/app/author/submissions/${submissionId}`)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể submit rebuttal')
    } finally {
      setSubmitting(false)
    }
  }

  if (loadingData) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!submissionId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing submission ID</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  // Robust canEdit check: true if no rebuttal exists, or if the rebuttal status is 'DRAFT' (case-insensitive)
  const canEdit = !rebuttal || (rebuttal.status && rebuttal.status.toUpperCase() === 'DRAFT')

  return (
    <CCard>
      <CCardHeader>
        <div className="d-flex justify-content-between align-items-center">
          <h4>
            {rebuttal ? 'Chỉnh sửa Rebuttal' : 'Tạo Rebuttal'}
            {rebuttal?.status === 'SUBMITTED' && (
              <CBadge color="success" className="ms-2">
                Đã submit
              </CBadge>
            )}
          </h4>
          {rebuttal && canEdit && (
            <CButton color="success" onClick={handleSubmit} disabled={submitting}>
              {submitting ? 'Đang submit...' : 'Submit Rebuttal'}
            </CButton>
          )}
        </div>
      </CCardHeader>
      <CCardBody>
        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        {rebuttal?.status === 'SUBMITTED' && (
          <CAlert color="info" className="mb-3">
            Rebuttal đã được submit. Bạn không thể chỉnh sửa.
          </CAlert>
        )}

        <CForm onSubmit={handleSave}>
          <div className="mb-3">
            <CFormLabel>
              Nội dung Rebuttal <span className="text-danger">*</span>
            </CFormLabel>
            <textarea
              className="form-control"
              value={content}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => {
                console.log('Content changing:', e.target.value);
                setContent(e.target.value);
              }}
              required
              rows={10}
              placeholder="Nhập nội dung rebuttal để phản hồi các reviews..."
              disabled={!canEdit}
            />
            <small className="text-muted">
              Rebuttal là phản hồi của bạn đối với các reviews. Hãy giải thích rõ ràng các điểm
              mà reviewers đã nêu ra.
            </small>
          </div>

          <div className="d-flex justify-content-end gap-2">
            <CButton color="secondary" onClick={() => navigate(-1)} disabled={loading}>
              Hủy
            </CButton>
            <CButton
              color="primary"
              type="submit"
              disabled={loading || !canEdit}
            >
              {loading ? <CSpinner size="sm" /> : 'Lưu bản nháp'}
            </CButton>
          </div>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default RebuttalFormPage
