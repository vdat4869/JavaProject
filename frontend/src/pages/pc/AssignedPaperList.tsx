import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
  CSpinner,
  CButton,
} from '@coreui/react'
import { reviewService, Assignment } from '../../services/review.service'

/**
 * AssignedPaperList - Danh sách papers được giao cho PC/Reviewer
 *
 * Features:
 * - Hiển thị assignments (Double-blind - không có author info)
 * - Filter theo status
 * - Actions: Review, View Discussion, Declare COI
 */
const AssignedPaperList: React.FC = () => {
  const navigate = useNavigate()
  const [assignments, setAssignments] = useState<Assignment[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadAssignments()
  }, [])

  const loadAssignments = async () => {
    try {
      setLoading(true)
      const data = await reviewService.getAssignments()
      setAssignments(data)
    } catch (error) {
      console.error('Error loading assignments:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusBadge = (status: Assignment['status']) => {
    const colorMap: Record<string, string> = {
      PENDING: 'secondary',
      IN_PROGRESS: 'warning',
      COMPLETED: 'success',
      DECLINED: 'danger',
    }
    return <CBadge color={colorMap[status] || 'secondary'}>{status}</CBadge>
  }

  const isDeadlinePassed = (deadline: string) => {
    return new Date(deadline) < new Date()
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
        <h4>Bài được giao</h4>
      </CCardHeader>
      <CCardBody>
        {assignments.length === 0 ? (
          <div className="text-center py-5">
            <p className="text-muted">Chưa có bài nào được giao</p>
          </div>
        ) : (
          <CTable hover>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>ID</CTableHeaderCell>
                <CTableHeaderCell>Tiêu đề</CTableHeaderCell>
                <CTableHeaderCell>Hội nghị</CTableHeaderCell>
                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                <CTableHeaderCell>Hạn chót</CTableHeaderCell>
                <CTableHeaderCell>COI</CTableHeaderCell>
                <CTableHeaderCell>Thao tác</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {assignments.map((assignment: Assignment) => (
                <CTableRow key={assignment.id}>
                  <CTableDataCell>{assignment.submissionId}</CTableDataCell>
                  <CTableDataCell>{assignment.submissionTitle}</CTableDataCell>
                  <CTableDataCell>{assignment.conferenceName}</CTableDataCell>
                  <CTableDataCell>{getStatusBadge(assignment.status)}</CTableDataCell>
                  <CTableDataCell>
                    {new Date(assignment.deadline).toLocaleDateString('vi-VN')}
                    {isDeadlinePassed(assignment.deadline) && (
                      <CBadge color="danger" className="ms-2">
                        Hết hạn
                      </CBadge>
                    )}
                  </CTableDataCell>
                  <CTableDataCell>
                    {assignment.hasCOI ? (
                      <CBadge color="warning">Có COI</CBadge>
                    ) : (
                      <CBadge color="success">Không</CBadge>
                    )}
                  </CTableDataCell>
                  <CTableDataCell>
                    {assignment.canReview && !isDeadlinePassed(assignment.deadline) && (
                      <>
                        {assignment.reviewId ? (
                          <CButton
                            color="link"
                            size="sm"
                            onClick={() => navigate(`/pc/reviews/${assignment.reviewId}/edit`)}
                          >
                            Sửa đánh giá
                          </CButton>
                        ) : (
                          <CButton
                            color="primary"
                            size="sm"
                            onClick={() =>
                              navigate(`/pc/reviews/new?assignmentId=${assignment.id}`)
                            }
                          >
                            Đánh giá
                          </CButton>
                        )}
                      </>
                    )}
                    {assignment.reviewId && (
                      <CButton
                        color="link"
                        size="sm"
                        onClick={() => navigate(`/pc/reviews/${assignment.reviewId}/discussion`)}
                      >
                        Thảo luận
                      </CButton>
                    )}
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => navigate(`/pc/coi?submissionId=${assignment.submissionId}`)}
                    >
                      Khai báo COI
                    </CButton>
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

export default AssignedPaperList
