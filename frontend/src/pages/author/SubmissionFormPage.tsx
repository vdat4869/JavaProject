import React, { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { CAlert, CCard, CCardBody, CCardHeader, CButton } from '@coreui/react'
import { submissionService } from '../../services/submission.service'
import { pcService } from '../../services/pc.service'
import { conferenceService } from '../../services/conference.service'
import { useAuth } from '../../context/AuthContext'
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
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const conferenceId = parseInt(searchParams.get('conferenceId') || '0')
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [isRestricted, setIsRestricted] = useState(false)
  const [restrictionReason, setRestrictionReason] = useState('')
  const [checkingRole, setCheckingRole] = useState(true)

  React.useEffect(() => {
    const checkRole = async () => {
      if (!conferenceId || !user) return

      try {
        setCheckingRole(true)
        // 1. Check if Chair
        const conference = await conferenceService.getConference(conferenceId)
        if (conference.chairId === user.id) {
          setIsRestricted(true)
          setRestrictionReason('Bạn không được phép nộp bài vào hội nghị mà bạn đang làm Chair.')
          return
        }

        // 2. Check if PC Member
        const membership = await pcService.getMyMembership(conferenceId)
        if (membership && membership.status === 'ACCEPTED') {
          setIsRestricted(true)
          setRestrictionReason('Bạn không được phép nộp bài vào hội nghị mà bạn đã chấp nhận tham gia hội đồng PC.')
          return
        }
      } catch (error) {
        console.error('Error checking role:', error)
      } finally {
        setCheckingRole(false)
      }
    }

    checkRole()
  }, [conferenceId, user])

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <p className="text-danger">Vui lòng chọn hội nghị</p>
          <CButton color="primary" onClick={() => navigate('/app/author')}>Quay lại</CButton>
        </CCardBody>
      </CCard>
    )
  }

  if (checkingRole) {
    return <div>Đang kiểm tra quyền hạn...</div>
  }

  if (isRestricted) {
    return (
      <CCard>
        <CCardHeader>
          <h4>Nộp bài mới</h4>
        </CCardHeader>
        <CCardBody>
          <CAlert color="warning" className="d-flex align-items-center">
            <div>
              <strong>Từ chối truy cập:</strong> {restrictionReason}
            </div>
          </CAlert>
          <div className="mt-3">
            <CButton color="secondary" onClick={() => navigate('/app/author/submissions')}>
              Quay lại danh sách bài nộp
            </CButton>
          </div>
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

      navigate('/app/author/submissions')
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
          onCancel={() => navigate('/app/author/submissions')}
          loading={loading}
        />
      </CCardBody>
    </CCard>
  )
}

export default SubmissionFormPage
