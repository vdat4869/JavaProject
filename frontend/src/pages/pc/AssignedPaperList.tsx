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
  CAlert,
} from '@coreui/react'
import { assignmentService, Assignment, AssignmentStatus } from '../../services/assignment.service'
import { reviewService } from '../../services/review.service'

/**
 * AssignedPaperList - Danh sách papers được giao cho PC/Reviewer
 *
 * Features:
 * - Hiển thị assignments (Double-blind - không có author info)
 * - Accept/Decline assignments
 * - Actions: Review, View Discussion, Declare COI
 */
const AssignedPaperList: React.FC = () => {
  const navigate = useNavigate()
  const [assignments, setAssignments] = useState<Assignment[]>([])
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadAssignments()
  }, [])

  const loadAssignments = async () => {
    try {
      setLoading(true)
      const data = await assignmentService.getMyAssignments()
      setAssignments(data)
    } catch (error: any) {
      console.error('Error loading assignments:', error)
      setError('Không thể tải danh sách assignments')
    } finally {
      setLoading(false)
    }
  }

  const handleAccept = async (assignmentId: number) => {
    try {
      setProcessing(assignmentId)
      setError('')
      setSuccess('')
      await assignmentService.acceptAssignment(assignmentId)
      setSuccess('Đã chấp nhận assignment thành công')
      await loadAssignments()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể chấp nhận assignment')
    } finally {
      setProcessing(null)
    }
  }

  const handleDecline = async (assignmentId: number) => {
    if (!window.confirm('Bạn có chắc chắn muốn từ chối assignment này?')) {
      return
    }

    try {
      setProcessing(assignmentId)
      setError('')
      setSuccess('')
      await assignmentService.declineAssignment(assignmentId)
      setSuccess('Đã từ chối assignment')
      await loadAssignments()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể từ chối assignment')
    } finally {
      setProcessing(null)
    }
  }

  const getStatusBadge = (status: AssignmentStatus) => {
    const colorMap: Record<AssignmentStatus, string> = {
      ASSIGNED: 'secondary',
      ACCEPTED: 'info',
      DECLINED: 'danger',
      COMPLETED: 'success',
    }
    const labelMap: Record<AssignmentStatus, string> = {
      ASSIGNED: 'Đã giao',
      ACCEPTED: 'Đã chấp nhận',
      DECLINED: 'Đã từ chối',
      COMPLETED: 'Đã hoàn thành',
    }
    return <CBadge color={colorMap[status]}>{labelMap[status]}</CBadge>
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
        {error && (
          <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
            {error}
          </CAlert>
        )}
        {success && (
          <CAlert color="success" className="mb-3" onClose={() => setSuccess('')} dismissible>
            {success}
          </CAlert>
        )}

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
                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                <CTableHeaderCell>Ngày giao</CTableHeaderCell>
                <CTableHeaderCell>Primary</CTableHeaderCell>
                <CTableHeaderCell>Thao tác</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {assignments.map((assignment: Assignment) => (
                <CTableRow key={assignment.id}>
                  <CTableDataCell>#{assignment.submissionId}</CTableDataCell>
                  <CTableDataCell>{assignment.submissionTitle}</CTableDataCell>
                  <CTableDataCell>{getStatusBadge(assignment.status)}</CTableDataCell>
                  <CTableDataCell>
                    {new Date(assignment.assignedAt).toLocaleDateString('vi-VN')}
                  </CTableDataCell>
                  <CTableDataCell>
                    {assignment.isPrimary && <CBadge color="primary">Primary</CBadge>}
                  </CTableDataCell>
                  <CTableDataCell>
                    <div className="d-flex gap-2">
                      {assignment.status === 'ASSIGNED' && (
                        <>
                          <CButton
                            color="success"
                            size="sm"
                            onClick={() => handleAccept(assignment.id)}
                            disabled={processing === assignment.id}
                          >
                            {processing === assignment.id ? (
                              <CSpinner size="sm" />
                            ) : (
                              'Chấp nhận'
                            )}
                          </CButton>
                          <CButton
                            color="danger"
                            size="sm"
                            onClick={() => handleDecline(assignment.id)}
                            disabled={processing === assignment.id}
                          >
                            {processing === assignment.id ? (
                              <CSpinner size="sm" />
                            ) : (
                              'Từ chối'
                            )}
                          </CButton>
                        </>
                      )}
                      <CButton
                        color="primary"
                        size="sm"
                        onClick={() =>
                          navigate(`/app/pc/submissions/${assignment.submissionId}`)
                        }
                      >
                        Chi tiết & Đánh giá
                      </CButton>
                      <CButton
                        color="link"
                        size="sm"
                        onClick={() => navigate(`/app/pc/coi?submissionId=${assignment.submissionId}`)}
                      >
                        Khai báo COI
                      </CButton>
                    </div>
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
