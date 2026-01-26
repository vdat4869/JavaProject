import React, { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CButton, CBadge, CSpinner, CAlert } from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilFile, cilPencil, cilTrash, cilCloudUpload, cilCheckCircle, cilHistory, cilArrowLeft } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { submissionService, Submission } from '../../services/submission.service'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'
import ReviewResultView from '../../components/submission/ReviewResultView'
import FileVersionHistory from '../../components/submission/FileVersionHistory'

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
  const [submitting, setSubmitting] = useState(false)

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
    if (!submission) return

    const isDraft = submission.status === 'DRAFT'
    const message = isDraft
      ? 'Bạn có chắc chắn muốn xóa bài nộp này?'
      : 'Bạn có chắc chắn muốn rút bài nộp này?'

    if (!window.confirm(message)) {
      return
    }

    try {
      if (isDraft) {
        await submissionService.deleteSubmission(parseInt(id!))
      } else {
        await submissionService.withdrawSubmission(parseInt(id!))
      }
      navigate('/app/author/submissions')
    } catch (error) {
      alert('Không thể thực hiện thao tác. Vui lòng thử lại.')
    }
  }

  const handleSubmit = async () => {
    if (!submission) return

    // Check if submission has PDF file
    if (!submission.pdfFilePath && !submission.fileUrl) {
      alert('Vui lòng upload file PDF trước khi nộp bài.')
      return
    }

    const message =
      'Bạn có chắc chắn muốn nộp bài này? Sau khi nộp, bạn sẽ không thể chỉnh sửa trừ khi rút bài.'

    if (!window.confirm(message)) {
      return
    }

    try {
      setSubmitting(true)
      const updated = await submissionService.submitSubmission(parseInt(id!))
      setSubmission(updated)
      alert('Bài nộp đã được gửi thành công!')
    } catch (error: any) {
      alert(error.response?.data?.message || 'Không thể nộp bài. Vui lòng thử lại.')
    } finally {
      setSubmitting(false)
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
              {submission.status === 'DRAFT' && (
                <CButton
                  color="success"
                  size="sm"
                  onClick={handleSubmit}
                  disabled={submitting || (!submission.pdfFilePath && !submission.fileUrl)}
                >
                  {submitting ? (
                    <>
                      <CSpinner size="sm" className="me-2" />
                      Đang nộp...
                    </>
                  ) : (
                    'Nộp bài'
                  )}
                </CButton>
              )}
              {submission.canEdit && (
                <CButton
                  color="primary"
                  size="sm"
                  onClick={() => navigate(`/app/author/submissions/${id}/edit`)}
                >
                  Sửa
                </CButton>
              )}
              <CButton
                color="secondary"
                size="sm"
                onClick={() => navigate('/app/author/submissions')}
              >
                <CIcon icon={cilArrowLeft} /> Quay lại
              </CButton>
              {submission.status === 'REVIEWED' && (
                <CButton
                  color="warning"
                  size="sm"
                  onClick={() => navigate(`/app/author/submissions/${id}/rebuttal`)}
                >
                  Gửi phản hồi (Rebuttal)
                </CButton>
              )}
              {(submission.status === 'ACCEPTED' || submission.status === 'CAMERA_READY') && (
                <CButton
                  color="success"
                  size="sm"
                  onClick={() => navigate(`/app/author/submissions/${id}/camera-ready`)}
                >
                  Nộp bản thảo cuối (Camera-ready)
                </CButton>
              )}
              {submission.canWithdraw && (
                <CButton color="danger" size="sm" onClick={handleWithdraw}>
                  {submission.status === 'DRAFT' ? 'Xóa' : 'Rút bài'}
                </CButton>
              )}
              {(submission.fileUrl || submission.pdfFilePath) && (
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
            <p>{submission.abstract || submission.abstractText || ''}</p>
          </div>

          {((submission.keywordsArray && submission.keywordsArray.length > 0) || submission.keywords) && (
            <div className="mb-3">
              <strong>Từ khóa: </strong>
              {submission.keywordsArray
                ? submission.keywordsArray.join(', ')
                : submission.keywords
                  ? submission.keywords.split(',').map((k: string) => k.trim()).join(', ')
                  : ''}
            </div>
          )}

          {submission.authors && submission.authors.length > 0 && (
            <div className="mb-3">
              <strong>Tác giả: </strong>
              <ul className="list-unstyled mt-2">
                {submission.authors.map((author, index) => (
                  <li key={index} className="mb-1">
                    {index + 1}. {author.firstName} {author.lastName}
                    {author.email && ` (${author.email})`}
                    {author.affiliation && ` - ${author.affiliation}`}
                    {author.isCorresponding && (
                      <CBadge color="success" className="ms-2">
                        Tác giả liên hệ
                      </CBadge>
                    )}
                  </li>
                ))}
              </ul>
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

      {/* File Version History */}
      <FileVersionHistory submissionId={submission.id} />

      {/* Reviews và Decision */}
      {(submission.status === 'REVIEWED' ||
        submission.status === 'ACCEPTED' ||
        submission.status === 'REJECTED') && <ReviewResultView submissionId={submission.id} />}
    </>
  )
}

export default SubmissionDetail
