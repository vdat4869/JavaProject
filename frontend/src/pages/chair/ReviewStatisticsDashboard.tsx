import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CRow,
  CCol,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { reviewService, ReviewStatistics, ReviewScore } from '../../services/review.service'

/**
 * ReviewStatisticsDashboard - Dashboard hiển thị thống kê review cho chairs
 *
 * Features:
 * - Review completion rate
 * - Average scores
 * - Score distribution
 * - Reviewer performance metrics
 * - Submission timeline
 */
const ReviewStatisticsDashboard: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [stats, setStats] = useState<ReviewStatistics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadStatistics()
    }
  }, [conferenceId])

  const loadStatistics = async () => {
    try {
      setLoading(true)
      const data = await reviewService.getReviewStatistics(conferenceId!)
      setStats(data)
    } catch (error: any) {
      console.error('Error loading review statistics:', error)
      setError(error.response?.data?.message || 'Không thể tải thống kê review')
    } finally {
      setLoading(false)
    }
  }

  const getScoreLabel = (score: string) => {
    const labels: Record<string, string> = {
      STRONG_ACCEPT: 'Chấp nhận mạnh mẽ',
      ACCEPT: 'Chấp nhận',
      WEAK_ACCEPT: 'Chấp nhận yếu',
      BORDERLINE: 'Ranh giới',
      WEAK_REJECT: 'Từ chối yếu',
      REJECT: 'Từ chối',
      STRONG_REJECT: 'Từ chối mạnh mẽ',
    }
    return labels[score] || score
  }

  const getScoreBadgeColor = (score: string) => {
    if (score.includes('ACCEPT')) return 'success'
    if (score.includes('REJECT')) return 'danger'
    return 'warning'
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
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

  if (error) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{error}</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  if (!stats) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="info">Không có dữ liệu thống kê</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      {/* Overall Statistics */}
      <CRow className="mb-4">
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Tỷ lệ hoàn thành</h6>
              <h3>{stats.completionRate.toFixed(1)}%</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Điểm trung bình</h6>
              <h3>{stats.averageScore.toFixed(2)}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Tổng số reviews</h6>
              <h3>{stats.totalReviews}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Thời gian hoàn thành TB</h6>
              <h3>{stats.averageCompletionTime.toFixed(1)} ngày</h3>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      {/* Review Status */}
      <CRow className="mb-4">
        <CCol md={6}>
          <CCard>
            <CCardHeader>
              <h6>Trạng thái Reviews</h6>
            </CCardHeader>
            <CCardBody>
              <div className="mb-2">
                <strong>Đã hoàn thành: </strong>
                <CBadge color="success">{stats.completedReviews}</CBadge>
              </div>
              <div className="mb-2">
                <strong>Đang chờ: </strong>
                <CBadge color="warning">{stats.pendingReviews}</CBadge>
              </div>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={6}>
          <CCard>
            <CCardHeader>
              <h6>Phân bố điểm</h6>
            </CCardHeader>
            <CCardBody>
              {Object.keys(stats.scoreDistribution).length === 0 ? (
                <p className="text-muted">Chưa có dữ liệu</p>
              ) : (
                <CTable hover small>
                  <CTableHead>
                    <CTableRow>
                      <CTableHeaderCell>Điểm</CTableHeaderCell>
                      <CTableHeaderCell>Số lượng</CTableHeaderCell>
                    </CTableRow>
                  </CTableHead>
                  <CTableBody>
                    {Object.entries(stats.scoreDistribution)
                      .sort((a, b) => b[1] - a[1])
                      .map(([score, count]) => (
                        <CTableRow key={score}>
                          <CTableDataCell>
                            <CBadge color={getScoreBadgeColor(score)}>
                              {getScoreLabel(score)}
                            </CBadge>
                          </CTableDataCell>
                          <CTableDataCell>
                            <strong>{count}</strong>
                          </CTableDataCell>
                        </CTableRow>
                      ))}
                  </CTableBody>
                </CTable>
              )}
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      {/* Reviewer Performance */}
      {Object.keys(stats.reviewerMetrics).length > 0 && (
        <CCard className="mb-4">
          <CCardHeader>
            <h5>Hiệu suất Reviewers</h5>
          </CCardHeader>
          <CCardBody>
            <CTable hover responsive>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Reviewer</CTableHeaderCell>
                  <CTableHeaderCell>Tổng số</CTableHeaderCell>
                  <CTableHeaderCell>Đã hoàn thành</CTableHeaderCell>
                  <CTableHeaderCell>Tỷ lệ hoàn thành</CTableHeaderCell>
                  <CTableHeaderCell>Điểm TB</CTableHeaderCell>
                  <CTableHeaderCell>Thời gian TB (ngày)</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {Object.values(stats.reviewerMetrics)
                  .sort((a, b) => b.completedReviews - a.completedReviews)
                  .map((reviewer) => (
                    <CTableRow key={reviewer.reviewerId}>
                      <CTableDataCell>{reviewer.reviewerName}</CTableDataCell>
                      <CTableDataCell>{reviewer.totalReviews}</CTableDataCell>
                      <CTableDataCell>
                        <CBadge color="success">{reviewer.completedReviews}</CBadge>
                      </CTableDataCell>
                      <CTableDataCell>
                        {reviewer.completionRate.toFixed(1)}%
                      </CTableDataCell>
                      <CTableDataCell>
                        {reviewer.averageScore.toFixed(2)}
                      </CTableDataCell>
                      <CTableDataCell>
                        {reviewer.averageCompletionTime.toFixed(1)}
                      </CTableDataCell>
                    </CTableRow>
                  ))}
              </CTableBody>
            </CTable>
          </CCardBody>
        </CCard>
      )}
    </>
  )
}

export default ReviewStatisticsDashboard
