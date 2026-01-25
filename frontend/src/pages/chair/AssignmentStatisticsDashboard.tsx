import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
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
import { assignmentService, AssignmentStatistics } from '../../services/assignment.service'

/**
 * AssignmentStatisticsDashboard - Dashboard hiển thị thống kê assignments
 */
const AssignmentStatisticsDashboard: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const conferenceId = id ? parseInt(id) : null
  const [stats, setStats] = useState<AssignmentStatistics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadData()
    }
  }, [conferenceId])

  const loadData = async () => {
    try {
      setLoading(true)
      setError('')
      const data = await assignmentService.getAssignmentStatistics(conferenceId!)
      setStats(data)
    } catch (error: any) {
      setError('Không thể tải thống kê assignments')
      console.error('Error loading assignment statistics:', error)
    } finally {
      setLoading(false)
    }
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

  const getWorkloadBadge = (status: string) => {
    const colorMap: Record<string, string> = {
      LOW: 'success',
      NORMAL: 'info',
      HIGH: 'warning',
      OVERLOADED: 'danger',
    }
    const labelMap: Record<string, string> = {
      LOW: 'Thấp',
      NORMAL: 'Bình thường',
      HIGH: 'Cao',
      OVERLOADED: 'Quá tải',
    }
    return <CBadge color={colorMap[status]}>{labelMap[status] || status}</CBadge>
  }

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing conference ID</CAlert>
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

  if (error) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{error}</CAlert>
          <CButton color="primary" onClick={loadData}>
            Thử lại
          </CButton>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Assignment Statistics</h4>
            <CButton color="secondary" onClick={() => navigate(-1)}>
              Quay lại
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {stats && (
            <>
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
                      <h5>Min/Max</h5>
                      <h2>
                        {stats.minAssignments} / {stats.maxAssignments}
                      </h2>
                    </CCardBody>
                  </CCard>
                </CCol>
              </CRow>

              <CRow className="mb-4">
                <CCol md={4}>
                  <CCard>
                    <CCardHeader>
                      <h6>Acceptance Rate</h6>
                    </CCardHeader>
                    <CCardBody>
                      <h2>{(stats.acceptanceRate * 100).toFixed(1)}%</h2>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={4}>
                  <CCard>
                    <CCardHeader>
                      <h6>Completion Rate</h6>
                    </CCardHeader>
                    <CCardBody>
                      <h2>{(stats.completionRate * 100).toFixed(1)}%</h2>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={4}>
                  <CCard>
                    <CCardHeader>
                      <h6>Decline Rate</h6>
                    </CCardHeader>
                    <CCardBody>
                      <h2>{(stats.declineRate * 100).toFixed(1)}%</h2>
                    </CCardBody>
                  </CCard>
                </CCol>
              </CRow>

              <CRow className="mb-4">
                <CCol md={6}>
                  <CCard>
                    <CCardHeader>
                      <h6>Status Distribution</h6>
                    </CCardHeader>
                    <CCardBody>
                      <CTable>
                        <CTableHead>
                          <CTableRow>
                            <CTableHeaderCell>Status</CTableHeaderCell>
                            <CTableHeaderCell>Count</CTableHeaderCell>
                            <CTableHeaderCell>Percentage</CTableHeaderCell>
                          </CTableRow>
                        </CTableHead>
                        <CTableBody>
                          {Object.entries(stats.statusDistribution || {}).map(
                            ([status, count]) => {
                              const percentage =
                                stats.totalAssignments > 0
                                  ? ((count / stats.totalAssignments) * 100).toFixed(1)
                                  : '0.0'
                              return (
                                <CTableRow key={status}>
                                  <CTableDataCell>{getStatusBadge(status)}</CTableDataCell>
                                  <CTableDataCell>{count}</CTableDataCell>
                                  <CTableDataCell>{percentage}%</CTableDataCell>
                                </CTableRow>
                              )
                            }
                          )}
                        </CTableBody>
                      </CTable>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={6}>
                  <CCard>
                    <CCardHeader>
                      <h6>Workload Distribution</h6>
                    </CCardHeader>
                    <CCardBody>
                      <CTable>
                        <CTableHead>
                          <CTableRow>
                            <CTableHeaderCell>Workload</CTableHeaderCell>
                            <CTableHeaderCell>Count</CTableHeaderCell>
                            <CTableHeaderCell>Percentage</CTableHeaderCell>
                          </CTableRow>
                        </CTableHead>
                        <CTableBody>
                          {Object.entries(stats.workloadDistribution || {}).map(
                            ([status, count]) => {
                              const percentage =
                                stats.totalReviewers > 0
                                  ? ((count / stats.totalReviewers) * 100).toFixed(1)
                                  : '0.0'
                              return (
                                <CTableRow key={status}>
                                  <CTableDataCell>{getWorkloadBadge(status)}</CTableDataCell>
                                  <CTableDataCell>{count}</CTableDataCell>
                                  <CTableDataCell>{percentage}%</CTableDataCell>
                                </CTableRow>
                              )
                            }
                          )}
                        </CTableBody>
                      </CTable>
                    </CCardBody>
                  </CCard>
                </CCol>
              </CRow>
            </>
          )}
        </CCardBody>
      </CCard>
    </>
  )
}

export default AssignmentStatisticsDashboard
