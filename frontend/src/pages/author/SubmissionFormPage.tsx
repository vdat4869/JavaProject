import React, { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { submissionService } from '../../services/submission.service'
import SubmissionForm from '../../components/submission/SubmissionForm'

/**
 * SubmissionFormPage - Trang tạo submission mới
 *
 * Features:
 * - Form để tạo submission
 * - Upload PDF
 * - Validation
 */
const SubmissionFormPage: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const conferenceId = parseInt(searchParams.get('conferenceId') || '0')
  const [loading, setLoading] = useState(false)

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <p className="text-danger">Vui lòng chọn hội nghị</p>
          <button onClick={() => navigate('/author')}>Quay lại</button>
        </CCardBody>
      </CCard>
    )
  }

  const handleSubmit = async (data: {
    title: string
    abstract: string
    keywords: string[]
    trackId?: number
    file?: File
    authors?: any[]
  }) => {
    try {
      setLoading(true)
      // Step 1: Create submission (JSON only, no file)
      const submission = await submissionService.createSubmission({
        conferenceId,
        title: data.title,
        abstractText: data.abstract,
        keywords: data.keywords.join(', '), // Convert array to string
        trackId: data.trackId,
        authors: data.authors,
      })

      // Step 2: Upload PDF file if provided
      if (data.file) {
        await submissionService.uploadPdf(submission.id, data.file)
      }

      navigate('/author/submissions')
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Không thể tạo submission')
    } finally {
      setLoading(false)
    }
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Nộp bài mới</h4>
      </CCardHeader>
      <CCardBody>
        <SubmissionForm
          conferenceId={conferenceId}
          onSubmit={handleSubmit}
          onCancel={() => navigate('/author/submissions')}
          loading={loading}
        />
      </CCardBody>
    </CCard>
  )
}

export default SubmissionFormPage
