import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CBadge,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { reviewService, Review, AverageScore, ReviewScore } from '../../services/review.service'
import { useAuth } from '../../context/AuthContext'

/**
 * SubmissionReviewsView - Hiển thị tất cả reviews của một submission
 *
 * Features:
 * - Display all reviews for submission
 * - Show average score
 * - Handle blind-review constraints (hide reviewer names for authors in double-blind)
 * - Show reviewer names for chairs/admins
 */
const SubmissionReviewsView: React.FC = () => {
  const { t } = useTranslation()
  const { user } = useAuth()
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const submissionId = id ? parseInt(id) : null
  const [reviews, setReviews] = useState<Review[]>([])
  const [averageScore, setAverageScore] = useState<AverageScore | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (submissionId) {
      loadData()
    }
  }, [submissionId])

  const loadData = async () => {
    try {
      setLoading(true)
      const [reviewsData, avgScoreData] = await Promise.all([
        reviewService.getReviewsBySubmission(submissionId!),
        reviewService.getAverageScore(submissionId!),
      ])
      setReviews(reviewsData)
      setAverageScore(avgScoreData)
    } catch (error: any) {
      console.error('Error loading reviews:', error)
      setError(error.response?.data?.message || 'Không thể tải reviews')
    } finally {
      setLoading(false)
    }
  }

  const getScoreLabel = (score: ReviewScore) => {
    const labels: Record<ReviewScore, string> = {
      STRONG_ACCEPT: 'Chấp nhận mạnh mẽ (7)',
      ACCEPT: 'Chấp nhận (6)',
      WEAK_ACCEPT: 'Chấp nhận yếu (5)',
      BORDERLINE: 'Ranh giới (4)',
      WEAK_REJECT: 'Từ chối yếu (3)',
      REJECT: 'Từ chối (2)',
      STRONG_REJECT: 'Từ chối mạnh mẽ (1)',
    }
    return labels[score]
  }

  const getScoreBadgeColor = (score: ReviewScore) => {
    if (score.includes('ACCEPT')) return 'success'
    if (score.includes('REJECT')) return 'danger'
    return 'warning'
  }

  const isChairOrAdmin = user?.roles.some((role) => role === 'CHAIR' || role === 'ADMIN') || false

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!submissionId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Missing submission ID</CAlert>
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

  return (
    <>
      {/* Average Score Card */}
      {averageScore && (
        <CCard className="mb-4">
          <CCardHeader>
            <h5>Điểm trung bình</h5>
          </CCardHeader>
          <CCardBody>
            <div className="d-flex align-items-center gap-3">
              <div>
                <h3>{averageScore.averageScore != null ? averageScore.averageScore.toFixed(2) : 'N/A'}</h3>
                <small className="text-muted">Dựa trên {averageScore.reviewCount} reviews</small>
              </div>
            </div>
          </CCardBody>
        </CCard>
      )}

      {/* Reviews List */}
      <CCard>
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h5>Danh sách Reviews</h5>
            <CBadge color="info">{reviews.length} reviews</CBadge>
          </div>
        </CCardHeader>
        <CCardBody>
          {reviews.length === 0 ? (
            <CAlert color="info">Chưa có review nào cho submission này</CAlert>
          ) : (
            <div className="space-y-3">
              {reviews.map((review) => (
                <CCard key={review.id} className="mb-3">
                  <CCardBody>
                    <div className="d-flex justify-content-between align-items-start mb-3">
                      <div>
                        <h6>
                          Review #{review.id}
                          {review.isConfidential && (
                            <CBadge color="warning" className="ms-2">
                              Bảo mật
                            </CBadge>
                          )}
                        </h6>
                        {isChairOrAdmin && review.reviewerName && (
                          <small className="text-muted">
                            Reviewer: <strong>{review.reviewerName}</strong>
                          </small>
                        )}
                        {!isChairOrAdmin && !review.reviewerName && (
                          <small className="text-muted">Reviewer: Ẩn danh (Double-blind)</small>
                        )}
                      </div>
                      <div className="text-end">
                        <CBadge color={getScoreBadgeColor(review.score)}>
                          {getScoreLabel(review.score)}
                        </CBadge>
                        <br />
                        <small className="text-muted">
                          {review.status === 'SUBMITTED' ? 'Đã submit' : 'Bản nháp'}
                        </small>
                      </div>
                    </div>

                    {review.summary && (
                      <div className="mb-2">
                        <strong>Tóm tắt: </strong>
                        <p className="mb-0">{review.summary}</p>
                      </div>
                    )}

                    {review.strengths && (
                      <div className="mb-2">
                        <strong>Điểm mạnh: </strong>
                        <p className="mb-0">{review.strengths}</p>
                      </div>
                    )}

                    {review.weaknesses && (
                      <div className="mb-2">
                        <strong>Điểm yếu: </strong>
                        <p className="mb-0">{review.weaknesses}</p>
                      </div>
                    )}

                    {review.comments && (
                      <div className="mb-2">
                        <strong>Nhận xét: </strong>
                        <p className="mb-0">{review.comments}</p>
                      </div>
                    )}

                    <div className="d-flex gap-3 mt-2">
                      {review.overallRating && (
                        <small>
                          <strong>Đánh giá tổng thể: </strong>
                          {review.overallRating}/5
                        </small>
                      )}
                      {review.confidence && (
                        <small>
                          <strong>Độ tin cậy: </strong>
                          {review.confidence}/5
                        </small>
                      )}
                      {review.submittedAt && (
                        <small className="text-muted">
                          Gửi vào: {new Date(review.submittedAt).toLocaleString('vi-VN')}
                        </small>
                      )}
                    </div>
                  </CCardBody>
                </CCard>
              ))}
            </div>
          )}
        </CCardBody>
      </CCard>
    </>
  )
}

export default SubmissionReviewsView
