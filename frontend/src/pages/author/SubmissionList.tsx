import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
  CSpinner,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { submissionService, Submission } from '../../services/submission.service'

/**
 * SubmissionList - Danh sách submissions của Author
 *
 * Features:
 * - Hiển thị tất cả submissions
 * - Filter theo status
 * - Actions: View, Edit, Withdraw
 */
const SubmissionList: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [submissions, setSubmissions] = useState<Submission[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadSubmissions()
  }, [])

  const loadSubmissions = async () => {
    try {
      setLoading(true)
      const data = await submissionService.getMySubmissions()
      setSubmissions(data)
    } catch (error) {
      console.error('Error loading submissions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleWithdraw = async (id: number) => {
    if (!window.confirm('Bạn có chắc chắn muốn rút bài nộp này?')) {
      return
    }

    try {
      await submissionService.deleteSubmission(id)
      await loadSubmissions()
    } catch (error) {
      console.error('Error withdrawing submission:', error)
      alert('Không thể rút bài nộp. Vui lòng thử lại.')
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

  return (
    <CCard>
      <CCardHeader>
        <div className="d-flex justify-content-between align-items-center">
          <h4>Danh sách bài nộp</h4>
          <CButton color="primary" onClick={() => navigate('/author/submissions/new')}>
            Nộp bài mới
          </CButton>
        </div>
      </CCardHeader>
      <CCardBody>
        {submissions.length === 0 ? (
          <div className="text-center py-5">
            <p className="text-muted">Chưa có bài nộp nào</p>
            <CButton color="primary" onClick={() => navigate('/author/submissions/new')}>
              Nộp bài đầu tiên
            </CButton>
          </div>
        ) : (
          <CTable hover>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>ID</CTableHeaderCell>
                <CTableHeaderCell>Tiêu đề</CTableHeaderCell>
                <CTableHeaderCell>Hội nghị</CTableHeaderCell>
                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                <CTableHeaderCell>Ngày nộp</CTableHeaderCell>
                <CTableHeaderCell>Thao tác</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {submissions.map((submission) => (
                <CTableRow key={submission.id}>
                  <CTableDataCell>{submission.id}</CTableDataCell>
                  <CTableDataCell>{submission.title}</CTableDataCell>
                  <CTableDataCell>{submission.conferenceName}</CTableDataCell>
                  <CTableDataCell>{getStatusBadge(submission.status)}</CTableDataCell>
                  <CTableDataCell>
                    {submission.submittedAt
                      ? new Date(submission.submittedAt).toLocaleDateString('vi-VN')
                      : '-'}
                  </CTableDataCell>
                  <CTableDataCell>
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => navigate(`/author/submissions/${submission.id}`)}
                    >
                      Xem
                    </CButton>
                    {submission.canEdit && (
                      <CButton
                        color="link"
                        size="sm"
                        onClick={() => navigate(`/author/submissions/${submission.id}/edit`)}
                      >
                        Sửa
                      </CButton>
                    )}
                    {submission.canWithdraw && (
                      <CButton color="link" size="sm" onClick={() => handleWithdraw(submission.id)}>
                        Rút
                      </CButton>
                    )}
                  </CTableDataCell>
                </CTableRow>
              ))}
            </CTableBody>
          </CTable>
        )}
      </CCardBody>
    </CCard>
  )
}

export default SubmissionList
