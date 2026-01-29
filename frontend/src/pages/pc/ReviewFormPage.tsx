import React, { useState } from 'react'
import { useNavigate, useSearchParams, useParams } from 'react-router-dom'
import { CCard, CCardHeader, CButton } from '@coreui/react'

import { reviewService, ReviewSubmitDTO } from '../../services/review.service'
import ReviewForm from '../../components/review/ReviewForm'

/**
 * ReviewFormPage - Trang tạo/chỉnh sửa review
 *
 * Features:
 * - Create new review từ assignment
 * - Edit existing review
 * - Submit review (finalize)
 */
const ReviewFormPage: React.FC = () => {

  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { id } = useParams<{ id?: string }>()
  const assignmentId = searchParams.get('assignmentId')
    ? parseInt(searchParams.get('assignmentId')!)
    : undefined
  const reviewId = id ? parseInt(id) : undefined
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (data: ReviewSubmitDTO) => {
    try {
      setLoading(true)

      // Use createOrUpdateDraft for both create and update
      await reviewService.createOrUpdateDraft(data)

      navigate('/app/pc/assignments')
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Không thể lưu review')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmitReview = async () => {
    if (!reviewId) return

    if (
      !window.confirm(
        'Bạn có chắc chắn muốn submit review này? Sau khi submit, bạn không thể chỉnh sửa.',
      )
    ) {
      return
    }

    try {
      setSubmitting(true)
      await reviewService.submitReview(reviewId)
      navigate('/app/pc/assignments')
    } catch (error: any) {
      alert(error.response?.data?.message || 'Không thể submit review')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <CCard>
      <CCardHeader>
        <div className="d-flex justify-content-between align-items-center">
          <h4>{reviewId ? 'Chỉnh sửa đánh giá' : 'Đánh giá bài báo'}</h4>
          {reviewId && (
            <CButton color="success" onClick={handleSubmitReview} disabled={submitting}>
              {submitting ? 'Đang submit...' : 'Submit Review'}
            </CButton>
          )}
        </div>
      </CCardHeader>
      <ReviewForm
        assignmentId={assignmentId}
        reviewId={reviewId}
        onSubmit={handleSubmit}
        onCancel={() => navigate('/app/pc/assignments')}
        loading={loading}
      />
    </CCard>
  )
}

export default ReviewFormPage
