import React, { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CAlert, CSpinner, CButton } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { submissionService, Submission } from '../../services/submission.service'
import SubmissionForm from '../../components/submission/SubmissionForm'

/**
 * SubmissionEdit - Trang chỉnh sửa submission
 *
 * Features:
 * - Edit submission (chỉ trước deadline)
 * - Upload PDF mới
 * - Disable edit sau deadline
 */
const SubmissionEdit: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const [submission, setSubmission] = useState<Submission | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
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

      // Kiểm tra nếu không thể edit
      if (!data.canEdit) {
        // Redirect về detail page
        navigate(`/app/author/submissions/${id}`)
      }
    } catch (error) {
      console.error('Error loading submission:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleFormSubmit = async (data: {
    title: string
    abstract: string
    keywords: string[]
    trackId?: number
    file?: File
    authors?: any[]
  }) => {
    try {
      setSaving(true)
      // Step 1: Update submission metadata (JSON only, no file)
      await submissionService.updateSubmission(parseInt(id!), {
        title: data.title,
        abstractText: data.abstract,
        keywords: data.keywords.join(', '), // Convert array to string
        trackId: data.trackId,
        authors: data.authors,
      })

      // Step 2: Upload PDF file if provided
      if (data.file) {
        await submissionService.uploadPdf(parseInt(id!), data.file)
      }

      // Reload submission after update
      const updated = await submissionService.getSubmission(parseInt(id!))
      setSubmission(updated)
      navigate(`/app/author/submissions/${id}`)
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Không thể cập nhật submission')
    } finally {
      setSaving(false)
    }
  }

  const handleFinalSubmit = async () => {
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
      navigate(`/app/author/submissions/${id}`)
    } catch (error: any) {
      alert(error.response?.data?.message || 'Không thể nộp bài. Vui lòng thử lại.')
    } finally {
      setSubmitting(false)
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
      <CCard>
        <CCardBody>
          <CAlert color="danger">Không tìm thấy submission</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <div className="d-flex justify-content-between align-items-center">
          <h4>Chỉnh sửa bài nộp</h4>
          {submission.status === 'DRAFT' && (
            <CButton
              color="success"
              size="sm"
              onClick={handleFinalSubmit}
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
        </div>
      </CCardHeader>
      <CCardBody>
        {submission.deadline && new Date(submission.deadline) < new Date() && (
          <CAlert color="warning">Hạn chót đã qua. Bạn không thể chỉnh sửa bài nộp này.</CAlert>
        )}
        {submission.status === 'DRAFT' && !submission.pdfFilePath && !submission.fileUrl && (
          <CAlert color="info" className="mb-3">
            Lưu ý: Bạn cần upload file PDF trước khi có thể nộp bài.
          </CAlert>
        )}
        <SubmissionForm
          initialData={{
            title: submission.title,
            abstract: submission.abstract || submission.abstractText || '',
            keywords: submission.keywordsArray || (submission.keywords ? submission.keywords.split(',').map((k: string) => k.trim()) : []),
            trackId: submission.trackId,
            authors: submission.authors || [],
          }}
          conferenceId={submission.conferenceId}
          onSubmit={handleFormSubmit}
          onCancel={() => navigate(`/app/author/submissions/${id}`)}
          loading={saving}
        />
      </CCardBody>
    </CCard>
  )
}

export default SubmissionEdit
