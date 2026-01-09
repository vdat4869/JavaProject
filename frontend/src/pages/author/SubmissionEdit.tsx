import React, { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CAlert, CSpinner } from '@coreui/react'
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
        navigate(`/author/submissions/${id}`)
      }
    } catch (error) {
      console.error('Error loading submission:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (data: {
    title: string
    abstract: string
    keywords: string[]
    trackId?: number
    file: File
  }) => {
    try {
      setSaving(true)
      await submissionService.updateSubmission(parseInt(id!), {
        title: data.title,
        abstract: data.abstract,
        keywords: data.keywords,
        trackId: data.trackId,
        file: data.file,
      })
      navigate(`/author/submissions/${id}`)
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Không thể cập nhật submission')
    } finally {
      setSaving(false)
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
        <h4>Chỉnh sửa bài nộp</h4>
      </CCardHeader>
      <CCardBody>
        {submission.deadline && new Date(submission.deadline) < new Date() && (
          <CAlert color="warning">Hạn chót đã qua. Bạn không thể chỉnh sửa bài nộp này.</CAlert>
        )}
        <SubmissionForm
          initialData={{
            title: submission.title,
            abstract: submission.abstract,
            keywords: submission.keywords,
            trackId: submission.trackId,
          }}
          conferenceId={submission.conferenceId}
          onSubmit={handleSubmit}
          onCancel={() => navigate(`/author/submissions/${id}`)}
          loading={saving}
        />
      </CCardBody>
    </CCard>
  )
}

export default SubmissionEdit
