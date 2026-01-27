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
import { assignmentService, AssignmentQualityMetrics as AssignmentQualityMetricsData } from '../../services/assignment.service'

/**
 * AssignmentQualityMetrics - Dashboard hiển thị quality metrics của assignments
 */
const AssignmentQualityMetrics: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const conferenceId = id ? parseInt(id) : null
  const [metrics, setMetrics] = useState<AssignmentQualityMetricsData | null>(null)
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
      const data = await assignmentService.getAssignmentQualityMetrics(conferenceId!)
      setMetrics(data)
    } catch (error: any) {
      setError('Không thể tải quality metrics')
      console.error('Error loading assignment quality metrics:', error)
    } finally {
      setLoading(false)
    }
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
            <h4>Assignment Quality Metrics</h4>
            <CButton color="secondary" onClick={() => navigate(-1)}>
              Quay lại
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {metrics && (
            <>
              <CRow className="mb-4">
                <CCol md={3}>
                  <CCard>
                    <CCardBody>
                      <h5>Average Review Score</h5>
                      <h2>{metrics.averageReviewScore.toFixed(2)}</h2>
                      <small className="text-muted">out of 7.0</small>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={3}>
                  <CCard>
                    <CCardBody>
                      <h5>Avg Completion Time</h5>
                      <h2>{metrics.averageReviewCompletionTime.toFixed(1)}</h2>
                      <small className="text-muted">days</small>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={3}>
                  <CCard>
                    <CCardBody>
                      <h5>Submission Rate</h5>
                      <h2>{(metrics.reviewSubmissionRate * 100).toFixed(1)}%</h2>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={3}>
                  <CCard>
                    <CCardBody>
                      <h5>Avg Reviewer Rating</h5>
                      <h2>{metrics.averageReviewerRating.toFixed(2)}</h2>
                    </CCardBody>
                  </CCard>
                </CCol>
              </CRow>

              <CRow className="mb-4">
                <CCol md={6}>
                  <CCard>
                    <CCardHeader>
                      <h6>Review Status</h6>
                    </CCardHeader>
                    <CCardBody>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Submitted:</span>
                        <strong>{metrics.totalReviewsSubmitted}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Pending:</span>
                        <strong>{metrics.totalReviewsPending}</strong>
                      </div>
                      <div className="d-flex justify-content-between">
                        <span>Total:</span>
                        <strong>
                          {metrics.totalReviewsSubmitted + metrics.totalReviewsPending}
                        </strong>
                      </div>
                    </CCardBody>
                  </CCard>
                </CCol>
                <CCol md={6}>
                  <CCard>
                    <CCardHeader>
                      <h6>Review Score Distribution</h6>
                    </CCardHeader>
                    <CCardBody>
                      <CTable>
                        <CTableHead>
                          <CTableRow>
                            <CTableHeaderCell>Score</CTableHeaderCell>
                            <CTableHeaderCell>Count</CTableHeaderCell>
                          </CTableRow>
                        </CTableHead>
                        <CTableBody>
                          {Object.entries(metrics.reviewScoreDistribution || {})
                            .sort((a, b) => parseFloat(b[0]) - parseFloat(a[0]))
                            .map(([score, count]) => (
                              <CTableRow key={score}>
                                <CTableDataCell>
                                  <CBadge color="primary">{score}</CBadge>
                                </CTableDataCell>
                                <CTableDataCell>{count}</CTableDataCell>
                              </CTableRow>
                            ))}
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

export default AssignmentQualityMetrics
