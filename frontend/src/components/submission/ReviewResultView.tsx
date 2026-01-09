import React, { useState, useEffect, useCallback } from 'react'
import { CCard, CCardBody, CCardHeader, CSpinner, CBadge, CAlert } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { submissionService, Review, Decision } from '../../services/submission.service'

/**
 * ReviewResultView Props
 */
interface ReviewResultViewProps {
  submissionId: number
}

/**
 * ReviewResultView - Component hiển thị reviews và decision (anonymized)
 *
 * Features:
 * - Hiển thị reviews (không hiển thị reviewer identity)
 * - Hiển thị decision
 * - Anonymized reviews theo rules
 */
const ReviewResultView: React.FC<ReviewResultViewProps> = ({ submissionId }) => {
  const { t } = useTranslation()
  const [reviews, setReviews] = useState<Review[]>([])
  const [decision, setDecision] = useState<Decision | null>(null)
  const [loading, setLoading] = useState(true)

  const loadData = useCallback(async () => {
    try {
      setLoading(true)
      const [reviewsData, decisionData] = await Promise.all([
        submissionService.getReviews(submissionId),
        submissionService.getDecision(submissionId),
      ])
      setReviews(reviewsData)
      setDecision(decisionData)
    } catch (error) {
      console.error('Error loading review data:', error)
    } finally {
      setLoading(false)
    }
  }, [submissionId])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const getRecommendationBadge = (recommendation: Review['recommendation']) => {
    const colorMap: Record<string, string> = {
      ACCEPT: 'success',
      REJECT: 'danger',
      MINOR_REVISION: 'warning',
      MAJOR_REVISION: 'warning',
    }
    return <CBadge color={colorMap[recommendation] || 'secondary'}>{recommendation}</CBadge>
  }

  const getDecisionBadge = (decision: Decision['decision']) => {
    const colorMap: Record<string, string> = {
      ACCEPT: 'success',
      REJECT: 'danger',
      MINOR_REVISION: 'warning',
      MAJOR_REVISION: 'warning',
    }
    return <CBadge color={colorMap[decision] || 'secondary'}>{decision}</CBadge>
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-3">
        <CSpinner color="primary" size="sm" />
      </div>
    )
  }

  return (
    <>
      {decision && (
        <CCard className="mb-3">
          <CCardHeader>
            <h5>Quyết định</h5>
          </CCardHeader>
          <CCardBody>
            <div className="mb-2">
              <strong>Kết quả: </strong>
              {getDecisionBadge(decision.decision)}
            </div>
            {decision.comments && (
              <div className="mb-2">
                <strong>Nhận xét: </strong>
                <p>{decision.comments}</p>
              </div>
            )}
            <small className="text-muted">
              Quyết định vào: {new Date(decision.decidedAt).toLocaleString('vi-VN')}
            </small>
          </CCardBody>
        </CCard>
      )}

      <CCard>
        <CCardHeader>
          <h5>Đánh giá ({reviews.length})</h5>
        </CCardHeader>
        <CCardBody>
          {reviews.length === 0 ? (
            <CAlert color="info">Chưa có đánh giá nào</CAlert>
          ) : (
            <div className="space-y-3">
              {reviews.map((review, index) => (
                <div key={review.id} className="border-bottom pb-3 mb-3">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <strong>Đánh giá #{index + 1}</strong>
                    {getRecommendationBadge(review.recommendation)}
                  </div>

                  <div className="mb-2">
                    <strong>Đánh giá tổng thể: </strong>
                    {review.overallRating}/5
                  </div>

                  <div className="mb-2">
                    <strong>Độ tin cậy: </strong>
                    {review.confidence}/5
                  </div>

                  {review.comments && (
                    <div className="mb-2">
                      <strong>Nhận xét: </strong>
                      <p className="mb-0">{review.comments}</p>
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

                  <small className="text-muted">
                    Gửi vào: {new Date(review.submittedAt).toLocaleString('vi-VN')}
                  </small>

                  {/* Note: Reviewer identity không được hiển thị theo rules */}
                </div>
              ))}
            </div>
          )}
        </CCardBody>
      </CCard>
    </>
  )
}

export default ReviewResultView
