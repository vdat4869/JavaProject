import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CRow,
  CCol,
  CSpinner,
  CAlert,
  CFormInput,
  CFormLabel,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from '@coreui/react'
import {
  assignmentService,
  AssignmentStats,
  AutoAssignmentRequest,
} from '../../services/assignment.service'

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
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [stats, setStats] = useState<AssignmentStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [showAutoAssignModal, setShowAutoAssignModal] = useState(false)
  const [reviewsPerSubmission, setReviewsPerSubmission] = useState(3)
  const [considerCOI, setConsiderCOI] = useState(true)
  const [autoAssigning, setAutoAssigning] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadData()
    }
  }, [conferenceId])

  const loadData = async () => {
    try {
      setLoading(true)
      const statsData = await assignmentService.getStats(conferenceId!)
      setStats(statsData)
    } catch (error) {
      console.error('Error loading assignment data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAutoAssign = async () => {
    try {
      setAutoAssigning(true)
      setError('')
      const result = await assignmentService.autoAssign({
        conferenceId: conferenceId!,
        reviewsPerSubmission,
        considerCOI,
      })
      setSuccess(
        `Đã tạo ${result.assignmentsCreated} assignments, cập nhật ${result.assignmentsUpdated} assignments`,
      )
      setShowAutoAssignModal(false)
      await loadData()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể thực hiện auto assignment')
    } finally {
      setAutoAssigning(false)
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

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Assignment Dashboard</h4>
            <CButton color="primary" onClick={() => setShowAutoAssignModal(true)}>
              Auto Assignment
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {error && (
            <CAlert color="danger" className="mb-3">
              {error}
            </CAlert>
          )}
          {success && (
            <CAlert color="success" className="mb-3">
              {success}
            </CAlert>
          )}

          {stats && (
            <CRow>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Tổng số bài nộp</h5>
                    <h2>{stats.totalSubmissions}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Đã gán</h5>
                    <h2>{stats.assignedSubmissions}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Chưa gán</h5>
                    <h2>{stats.unassignedSubmissions}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
              <CCol md={3}>
                <CCard>
                  <CCardBody>
                    <h5>Đã review</h5>
                    <h2>{stats.completedReviews}</h2>
                  </CCardBody>
                </CCard>
              </CCol>
            </CRow>
          )}

          {stats && (
            <div className="mt-4">
              <h5>Thống kê</h5>
              <p>Đang chờ review: {stats.pendingReviews}</p>
              <p>Trung bình reviews/bài: {stats.averageReviewsPerSubmission.toFixed(2)}</p>
            </div>
          )}
        </CCardBody>
      </CCard>

      {/* Auto Assignment Modal */}
      <CModal visible={showAutoAssignModal} onClose={() => setShowAutoAssignModal(false)}>
        <CModalHeader>
          <CModalTitle>Auto Assignment</CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="mb-3">
            <CFormLabel>Số reviewers mỗi bài</CFormLabel>
            <CFormInput
              type="number"
              min="1"
              max="10"
              value={reviewsPerSubmission}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setReviewsPerSubmission(parseInt(e.target.value))
              }
            />
          </div>
          <div className="mb-3">
            <input
              type="checkbox"
              id="considerCOI"
              checked={considerCOI}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setConsiderCOI(e.target.checked)
              }
              className="form-check-input"
            />
            <label htmlFor="considerCOI" className="form-check-label ms-2">
              Xem xét Conflict of Interest
            </label>
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowAutoAssignModal(false)}>
            Hủy
          </CButton>
          <CButton color="primary" onClick={handleAutoAssign} disabled={autoAssigning}>
            {autoAssigning ? <CSpinner size="sm" /> : 'Thực hiện'}
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default AssignmentDashboard
