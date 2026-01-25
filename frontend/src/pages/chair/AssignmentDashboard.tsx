import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CRow,
  CCol,
  CSpinner,
  CAlert,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
} from '@coreui/react'
import {
  assignmentService,
  AssignmentStatistics,
  Assignment,
  AutoAssignResponse,
} from '../../services/assignment.service'
import ManualAssignmentForm from '../../components/assignment/ManualAssignmentForm'
import AutoAssignWithSuggestions from '../../components/assignment/AutoAssignWithSuggestions'
import ReassignmentModal from '../../components/assignment/ReassignmentModal'

/**
 * AssignmentDashboard - Dashboard quản lý assignments
 *
 * Features:
 * - Thống kê assignments
 * - Auto assignment
 * - Manual assignment
 * - Track review progress
 */
const AssignmentDashboard: React.FC = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [stats, setStats] = useState<AssignmentStatistics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Modal states
  const [showManualAssignModal, setShowManualAssignModal] = useState(false)
  const [showAutoAssignModal, setShowAutoAssignModal] = useState(false)
  const [showReassignModal, setShowReassignModal] = useState(false)
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<number | null>(null)
  const [selectedAssignment, setSelectedAssignment] = useState<Assignment | null>(null)
  const [assignments, setAssignments] = useState<Assignment[]>([])

  useEffect(() => {
    if (conferenceId) {
      loadData()
    }
  }, [conferenceId])

  const loadData = async () => {
    try {
      setLoading(true)
      setError('')
      const statsData = await assignmentService.getAssignmentStatistics(conferenceId!)
      setStats(statsData)
    } catch (error: any) {
      console.error('Error loading assignment data:', error)
      setError('Không thể tải thống kê assignments')
    } finally {
      setLoading(false)
    }
  }

  const handleManualAssign = (submissionId: number) => {
    setSelectedSubmissionId(submissionId)
    setShowManualAssignModal(true)
  }

  const handleAutoAssign = (submissionId: number) => {
    setSelectedSubmissionId(submissionId)
    setShowAutoAssignModal(true)
  }

  const handleReassign = (assignment: Assignment) => {
    setSelectedAssignment(assignment)
    setShowReassignModal(true)
  }

  const handleAssignmentSuccess = () => {
    setSuccess('Assignment đã được tạo thành công')
    loadData()
    if (selectedSubmissionId) {
      loadAssignments(selectedSubmissionId)
    }
  }

  const handleAutoAssignSuccess = (result: AutoAssignResponse) => {
    setSuccess(
      `Đã tạo ${result.totalCreated} assignments thành công. ${result.totalFailed > 0 ? `${result.totalFailed} assignments thất bại.` : ''}`
    )
    loadData()
    if (selectedSubmissionId) {
      loadAssignments(selectedSubmissionId)
    }
  }

  const loadAssignments = async (submissionId: number) => {
    try {
      const data = await assignmentService.getAssignmentsBySubmission(submissionId)
      setAssignments(data)
    } catch (error) {
      console.error('Error loading assignments:', error)
    }
  }

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing conferenceId</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  const getStatusBadge = (status: string) => {
    const colorMap: Record<string, string> = {
      ASSIGNED: 'secondary',
      ACCEPTED: 'info',
      DECLINED: 'danger',
      COMPLETED: 'success',
    }
    const labelMap: Record<string, string> = {
      ASSIGNED: 'Đã giao',
      ACCEPTED: 'Đã chấp nhận',
      DECLINED: 'Đã từ chối',
      COMPLETED: 'Đã hoàn thành',
    }
    return <CBadge color={colorMap[status]}>{labelMap[status] || status}</CBadge>
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Assignment Dashboard</h4>
            <div className="d-flex gap-2">
              <CButton
                color="secondary"
                onClick={() =>
                  navigate(`/app/chair/conference/${conferenceId}/assignment-statistics`)
                }
              >
                Xem Statistics
              </CButton>
              <CButton
                color="info"
                onClick={() =>
                  navigate(`/app/chair/conference/${conferenceId}/assignment-quality`)
                }
              >
                Xem Quality Metrics
              </CButton>
            </div>
          </div>
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

          {stats && (
            <CRow className="mb-4">
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Tổng Assignments</h5>
                    <h2>{stats.totalAssignments}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Tổng Reviewers</h5>
                    <h2>{stats.totalReviewers}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Trung bình/Reviewer</h5>
                    <h2>{stats.averageAssignmentsPerReviewer.toFixed(1)}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Acceptance Rate</h5>
                    <h2>{(stats.acceptanceRate * 100).toFixed(1)}%</h2>
                  </CCardBody>
                </CCard>
              </CCol>
            </CRow>
          )}

          {stats && (
            <CRow className="mb-4">
              <CCol md={6}>
                <CCard>
                  <CCardHeader>
                    <h6>Status Distribution</h6>
                  </CCardHeader>
                  <CCardBody>
                    {Object.entries(stats.statusDistribution || {}).map(([status, count]) => (
                      <div key={status} className="d-flex justify-content-between mb-2">
                        <span>{getStatusBadge(status)}</span>
                        <strong>{count}</strong>
                      </div>
                    ))}
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={6}>
                <CCard>
                  <CCardHeader>
                    <h6>Workload Distribution</h6>
                  </CCardHeader>
                  <CCardBody>
                    {Object.entries(stats.workloadDistribution || {}).map(([status, count]) => (
                      <div key={status} className="d-flex justify-content-between mb-2">
                        <span>
                          <CBadge color={status === 'OVERLOADED' ? 'danger' : 'info'}>
                            {status}
                          </CBadge>
                        </span>
                        <strong>{count}</strong>
                      </div>
                    ))}
                  </CCardBody>
                </CCard>
              </CCol>
            </CRow>
          )}

          <div className="mb-3">
            <h5>Quản lý Assignments</h5>
            <p className="text-muted">
              Để tạo assignment cho một submission cụ thể, vui lòng vào trang chi tiết submission
              và sử dụng các nút "Manual Assign" hoặc "Auto Assign".
            </p>
          </div>
        </CCardBody>
      </CCard>

      {/* Manual Assignment Modal */}
      {selectedSubmissionId && (
        <ManualAssignmentForm
          visible={showManualAssignModal}
          onClose={() => {
            setShowManualAssignModal(false)
            setSelectedSubmissionId(null)
          }}
          submissionId={selectedSubmissionId}
          conferenceId={conferenceId!}
          onSuccess={handleAssignmentSuccess}
        />
      )}

      {/* Auto Assign with Suggestions Modal */}
      {selectedSubmissionId && (
        <AutoAssignWithSuggestions
          visible={showAutoAssignModal}
          onClose={() => {
            setShowAutoAssignModal(false)
            setSelectedSubmissionId(null)
          }}
          submissionId={selectedSubmissionId}
          onSuccess={handleAutoAssignSuccess}
        />
      )}

      {/* Reassignment Modal */}
      {selectedAssignment && (
        <ReassignmentModal
          visible={showReassignModal}
          onClose={() => {
            setShowReassignModal(false)
            setSelectedAssignment(null)
          }}
          assignment={selectedAssignment}
          conferenceId={conferenceId!}
          onSuccess={handleAssignmentSuccess}
        />
      )}
    </>
  )
}

export default AssignmentDashboard
