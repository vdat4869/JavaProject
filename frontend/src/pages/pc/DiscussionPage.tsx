import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CSpinner, CButton, CAlert } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { reviewService, Review, Rebuttal } from '../../services/review.service'
import DiscussionThread from '../../components/review/DiscussionThread'

/**
 * DiscussionPage - Trang thảo luận nội bộ cho review
 *
 * Features:
 * - Hiển thị review info
 * - Discussion thread
 * - Rebuttal view (nếu có)
 */
const DiscussionPage: React.FC = () => {
  const { t } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [review, setReview] = useState<Review | null>(null)
  const [rebuttal, setRebuttal] = useState<Rebuttal | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (id) {
      loadReview()
    }
  }, [id])

  const loadReview = async () => {
    try {
      setLoading(true)
      const reviewData = await reviewService.getReview(parseInt(id!))
      setReview(reviewData)

      // Load rebuttal nếu có
      try {
        const rebuttalData = await reviewService.getRebuttal(reviewData.submissionId)
        setRebuttal(rebuttalData)
      } catch (error) {
        // Rebuttal không tồn tại, không cần xử lý
        throw error
      }
    } catch (error) {
      console.error('Error loading review:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  if (!review) {
    return (
      <CCard>
        <CCardBody>
          <p className="text-danger">Không tìm thấy review</p>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Thảo luận đánh giá</h4>
            <CButton color="secondary" onClick={() => navigate('/pc/assignments')}>
              Quay lại
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          <div className="mb-2">
            <strong>Đánh giá tổng thể: </strong>
            {review.overallRating}/5
          </div>
          <div className="mb-2">
            <strong>Độ tin cậy: </strong>
            {review.confidence}/5
          </div>
          <div className="mb-2">
            <strong>Khuyến nghị: </strong>
            {review.recommendation}
          </div>
          {review.comments && (
            <div className="mb-2">
              <strong>Nhận xét: </strong>
              <p className="mb-0">{review.comments}</p>
            </div>
          )}
        </CCardBody>
      </CCard>

      {/* Rebuttal View */}
      {rebuttal && (
        <CCard className="mb-3">
          <CCardHeader>
            <h5>Rebuttal từ Author</h5>
          </CCardHeader>
          <CCardBody>
            <p>{rebuttal.content}</p>
            <small className="text-muted">
              Gửi vào: {new Date(rebuttal.submittedAt).toLocaleString('vi-VN')}
            </small>
          </CCardBody>
        </CCard>
      )}

      <DiscussionThread reviewId={review.id} />
    </>
  )
}

export default DiscussionPage
