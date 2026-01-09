import React, { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CButton, CBadge, CSpinner, CAlert } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { submissionService, Submission } from '../../services/submission.service'
import ReviewResultView from '../../components/submission/ReviewResultView'

/**
 * SubmissionDetail - Trang chi tiết submission
 *
 * Features:
 * - Hiển thị thông tin submission
 * - Download PDF
 * - View reviews (anonymized)
 * - View decision
 * - Edit/Withdraw actions (nếu có quyền)
 */
const SubmissionDetail: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const [submission, setSubmission] = useState<Submission | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (id) {
      loadSubmission()
    }
  }, [id])

  const loadSubmission = async () => {
    try {
      setLoading(true)
      const data = await submissionService.getSubmission(parseInt(id!))
      setSubmission(data)
    } catch (error) {
      console.error('Error loading submission:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleWithdraw = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn rút bài nộp này?')) {
      return
    }

    try {
      await submissionService.deleteSubmission(parseInt(id!))
      navigate('/author/submissions')
    } catch (error) {
      alert('Không thể rút bài nộp. Vui lòng thử lại.')
    }
  }

  const handleDownloadFile = async () => {
    try {
      const blob = await submissionService.downloadFile(parseInt(id!))
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = submission?.fileName || `submission-${id}.pdf`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (error) {
      alert('Không thể tải file. Vui lòng thử lại.')
    }
  }

  const getStatusBadge = (status: Submission['status']) => {
    const colorMap: Record<string, string> = {
      DRAFT: 'secondary',
      SUBMITTED: 'info',
      UNDER_REVIEW: 'warning',
      REVIEWED: 'primary',
      ACCEPTED: 'success',
      REJECTED: 'danger',
      WITHDRAWN: 'dark',
      CAMERA_READY: 'success',
    }
    return <CBadge color={colorMap[status] || 'secondary'}>{status}</CBadge>
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
      <CCard>
        <CCardBody>
          <CAlert color="danger">Không tìm thấy submission</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>{submission.title}</h4>
            <div className="d-flex gap-2">
              {submission.canEdit && (
                <CButton
                  color="primary"
                  size="sm"
                  onClick={() => navigate(`/author/submissions/${id}/edit`)}
                >
                  Sửa
                </CButton>
              )}
              {submission.canWithdraw && (
                <CButton color="danger" size="sm" onClick={handleWithdraw}>
                  Rút bài
                </CButton>
              )}
              {submission.fileUrl && (
                <CButton color="secondary" size="sm" onClick={handleDownloadFile}>
                  Tải PDF
                </CButton>
              )}
            </div>
          </div>
        </CCardHeader>
        <CCardBody>
          <div className="mb-3">
            <strong>Trạng thái: </strong>
            {getStatusBadge(submission.status)}
          </div>

          <div className="mb-3">
            <strong>Hội nghị: </strong>
            {submission.conferenceName}
          </div>

          {submission.trackName && (
            <div className="mb-3">
              <strong>Lĩnh vực: </strong>
              {submission.trackName}
            </div>
          )}

          <div className="mb-3">
            <strong>Tóm tắt: </strong>
            <p>{submission.abstract}</p>
          </div>

          {submission.keywords && submission.keywords.length > 0 && (
            <div className="mb-3">
              <strong>Từ khóa: </strong>
              {submission.keywords.join(', ')}
            </div>
          )}

          {submission.submittedAt && (
            <div className="mb-3">
              <strong>Ngày nộp: </strong>
              {new Date(submission.submittedAt).toLocaleString('vi-VN')}
            </div>
          )}

          {submission.deadline && (
            <div className="mb-3">
              <strong>Hạn chót: </strong>
              {new Date(submission.deadline).toLocaleString('vi-VN')}
              {new Date(submission.deadline) < new Date() && (
                <CBadge color="danger" className="ms-2">
                  Đã hết hạn
                </CBadge>
              )}
            </div>
          )}

          {!submission.canEdit && submission.deadline && (
            <CAlert color="warning" className="mt-3">
              Hạn chót đã qua. Bạn không thể chỉnh sửa bài nộp này.
            </CAlert>
          )}
        </CCardBody>
      </CCard>

      {/* Reviews và Decision */}
      {(submission.status === 'REVIEWED' ||
        submission.status === 'ACCEPTED' ||
        submission.status === 'REJECTED') && <ReviewResultView submissionId={submission.id} />}
    </>
  )
}

export default SubmissionDetail
