import React, { useState, useEffect, useCallback } from 'react'
import {
  CForm,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CButton,
  CAlert,
  CSpinner,
  CCard,
  CCardBody,
  CBadge,
} from '@coreui/react'
import { reviewService, Review, Assignment } from '../../services/review.service'

/**
 * ReviewForm Props
 */
interface ReviewFormProps {
  assignmentId?: number
  reviewId?: number
  onSubmit: (data: {
    overallRating: number
    confidence: number
    comments: string
    strengths: string
    weaknesses: string
    recommendation: 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  }) => Promise<void>
  onCancel: () => void
  loading?: boolean
}

/**
 * ReviewForm - Form component cho create/edit review
 *
 * Features:
 * - Overall rating (1-5)
 * - Confidence (1-5)
 * - Comments, Strengths, Weaknesses
 * - Recommendation
 * - Validation
 * - Double-blind UI (không hiển thị author)
 */
const ReviewForm: React.FC<ReviewFormProps> = ({
  assignmentId,
  reviewId,
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const [overallRating, setOverallRating] = useState(3)
  const [confidence, setConfidence] = useState(3)
  const [comments, setComments] = useState('')
  const [strengths, setStrengths] = useState('')
  const [weaknesses, setWeaknesses] = useState('')
  const [recommendation, setRecommendation] = useState<
    'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  >('ACCEPT')
  const [assignment, setAssignment] = useState<Assignment | null>(null)
  const [review, setReview] = useState<Review | null>(null)
  const [loadingData, setLoadingData] = useState(false)
  const [error, setError] = useState('')

  const loadReview = useCallback(async () => {
    try {
      setLoadingData(true)
      const reviewData = await reviewService.getReview(reviewId!)
      setReview(reviewData)

      // Populate form với review data
      setOverallRating(reviewData.overallRating)
      setConfidence(reviewData.confidence)
      setComments(reviewData.comments)
      setStrengths(reviewData.strengths)
      setWeaknesses(reviewData.weaknesses)
      setRecommendation(reviewData.recommendation)

      // Load assignment để check deadline
      const assignmentData = await reviewService.getAssignment(reviewData.assignmentId)
      setAssignment(assignmentData)
    } catch (error) {
      console.error('Error loading review:', error)
      setError('Không thể tải thông tin đánh giá')
    } finally {
      setLoadingData(false)
    }
  }, [reviewId])

  const loadAssignment = useCallback(async () => {
    try {
      setLoadingData(true)
      const assignmentData = await reviewService.getAssignment(assignmentId!)
      setAssignment(assignmentData)
    } catch (error) {
      console.error('Error loading assignment:', error)
      setError('Không thể tải thông tin assignment')
    } finally {
      setLoadingData(false)
    }
  }, [assignmentId])

  useEffect(() => {
    if (reviewId) {
      void loadReview()
    } else if (assignmentId) {
      void loadAssignment()
    }
  }, [reviewId, assignmentId, loadReview, loadAssignment])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!comments.trim()) {
      setError('Vui lòng nhập nhận xét')
      return
    }

    if (!strengths.trim()) {
      setError('Vui lòng nhập điểm mạnh')
      return
    }

    if (!weaknesses.trim()) {
      setError('Vui lòng nhập điểm yếu')
      return
    }

    try {
      await onSubmit({
        overallRating,
        confidence,
        comments: comments.trim(),
        strengths: strengths.trim(),
        weaknesses: weaknesses.trim(),
        recommendation,
      })
    } catch (err: any) {
      setError(err.message || 'Có lỗi xảy ra')
    }
  }

  const isDeadlinePassed = assignment?.deadline ? new Date(assignment.deadline) < new Date() : false
  const canEdit = review ? review.canEdit : true

  if (loadingData) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  return (
    <CCard>
      <CCardBody>
        {assignment && (
          <div className="mb-4">
            <h5>Bài báo: {assignment.submissionTitle}</h5>
            <p className="text-muted">{assignment.submissionAbstract}</p>
            <p>
              <strong>Hạn chót: </strong>
              {new Date(assignment.deadline).toLocaleString('vi-VN')}
              {isDeadlinePassed && (
                <CBadge color="danger" className="ms-2">
                  Đã hết hạn
                </CBadge>
              )}
            </p>
          </div>
        )}

        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}

        {(isDeadlinePassed || !canEdit) && (
          <CAlert color="warning" className="mb-3">
            Hạn chót đã qua hoặc review đã được submit. Bạn không thể chỉnh sửa.
          </CAlert>
        )}

        <CForm onSubmit={handleSubmit}>
          <div className="mb-3">
            <CFormLabel>Đánh giá tổng thể (1-5) *</CFormLabel>
            <CFormInput
              type="number"
              min="1"
              max="5"
              value={overallRating}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setOverallRating(parseInt(e.target.value))
              }
              required
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Độ tin cậy (1-5) *</CFormLabel>
            <CFormInput
              type="number"
              min="1"
              max="5"
              value={confidence}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setConfidence(parseInt(e.target.value))
              }
              required
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Nhận xét *</CFormLabel>
            <CFormTextarea
              value={comments}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setComments(e.target.value)}
              required
              rows={5}
              placeholder="Nhập nhận xét về bài báo"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Điểm mạnh *</CFormLabel>
            <CFormTextarea
              value={strengths}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setStrengths(e.target.value)}
              required
              rows={3}
              placeholder="Nhập các điểm mạnh của bài báo"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Điểm yếu *</CFormLabel>
            <CFormTextarea
              value={weaknesses}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setWeaknesses(e.target.value)
              }
              required
              rows={3}
              placeholder="Nhập các điểm yếu của bài báo"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Khuyến nghị *</CFormLabel>
            <select
              className="form-select"
              value={recommendation}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setRecommendation(
                  e.target.value as 'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION',
                )
              }
              required
              disabled={isDeadlinePassed || !canEdit}
            >
              <option value="ACCEPT">Chấp nhận</option>
              <option value="MINOR_REVISION">Sửa đổi nhỏ</option>
              <option value="MAJOR_REVISION">Sửa đổi lớn</option>
              <option value="REJECT">Từ chối</option>
            </select>
          </div>

          <div className="d-flex justify-content-end gap-2">
            <CButton color="secondary" onClick={onCancel} disabled={loading}>
              Hủy
            </CButton>
            <CButton
              color="primary"
              type="submit"
              disabled={loading || isDeadlinePassed || !canEdit}
            >
              {loading ? (
                <CSpinner size="sm" />
              ) : review?.status === 'DRAFT' ? (
                'Lưu bản nháp'
              ) : (
                'Lưu'
              )}
            </CButton>
          </div>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default ReviewForm
