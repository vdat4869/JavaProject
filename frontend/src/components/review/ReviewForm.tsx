import React, { useState, useEffect, useCallback } from 'react'
import {
  CForm,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CFormSelect,
  CFormCheck,
  CButton,
  CAlert,
  CSpinner,
  CCard,
  CCardBody,
  CBadge,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import {
  reviewService,
  Review,
  Assignment,
  ReviewScore,
  ReviewSubmitDTO,
} from '../../services/review.service'

/**
 * ReviewForm Props
 */
interface ReviewFormProps {
  assignmentId?: number
  reviewId?: number
  onSubmit: (data: ReviewSubmitDTO) => Promise<void>
  onCancel: () => void
  loading?: boolean
}

/**
 * ReviewForm - Form component cho create/edit review
 *
 * Features:
 * - Summary (required)
 * - Score (ReviewScore enum: STRONG_ACCEPT to STRONG_REJECT)
 * - Comments, Strengths, Weaknesses
 * - Overall rating (1-5, optional)
 * - Confidence (1-5, optional)
 * - Is Confidential checkbox
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
  const { t } = useTranslation()
  const [summary, setSummary] = useState('')
  const [score, setScore] = useState<ReviewScore>('BORDERLINE')
  const [comments, setComments] = useState('')
  const [strengths, setStrengths] = useState('')
  const [weaknesses, setWeaknesses] = useState('')
  const [overallRating, setOverallRating] = useState<number | undefined>(undefined)
  const [confidence, setConfidence] = useState<number | undefined>(undefined)
  const [isConfidential, setIsConfidential] = useState(false)
  const [assignment, setAssignment] = useState<Assignment | null>(null)
  const [review, setReview] = useState<Review | null>(null)
  const [loadingData, setLoadingData] = useState(false)
  const [error, setError] = useState('')

  const loadReview = useCallback(async () => {
    try {
      setLoadingData(true)
      let reviewData: Review | null = null

      if (reviewId) {
        reviewData = await reviewService.getReview(reviewId)
      } else if (assignmentId) {
        // Try to load existing review by assignment
        reviewData = await reviewService.getReviewByAssignment(assignmentId)
      }

      if (reviewData) {
        setReview(reviewData)
        // Populate form với review data
        setSummary(reviewData.summary || '')
        setScore(reviewData.score)
        setComments(reviewData.comments || '')
        setStrengths(reviewData.strengths || '')
        setWeaknesses(reviewData.weaknesses || '')
        setOverallRating(reviewData.overallRating)
        setConfidence(reviewData.confidence)
        setIsConfidential(reviewData.isConfidential || false)

        // Load assignment để check deadline
        const assignmentData = await reviewService.getAssignment(reviewData.assignmentId)
        setAssignment(assignmentData)
      } else if (assignmentId) {
        // No existing review, load assignment for new review
        const assignmentData = await reviewService.getAssignment(assignmentId)
        setAssignment(assignmentData)
      }
    } catch (error) {
      console.error('Error loading review:', error)
      setError('Không thể tải thông tin đánh giá')
    } finally {
      setLoadingData(false)
    }
  }, [reviewId, assignmentId])

  useEffect(() => {
    void loadReview()
  }, [loadReview])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!summary.trim()) {
      setError('Vui lòng nhập tóm tắt')
      return
    }

    if (!comments.trim()) {
      setError('Vui lòng nhập nhận xét')
      return
    }

    if (!assignmentId && !review?.assignmentId) {
      setError('Missing assignment ID')
      return
    }

    try {
      const submitData: ReviewSubmitDTO = {
        assignmentId: assignmentId || review!.assignmentId,
        summary: summary.trim(),
        comments: comments.trim(),
        strengths: strengths.trim() || undefined,
        weaknesses: weaknesses.trim() || undefined,
        score,
        isConfidential,
        overallRating,
        confidence,
      }

      await onSubmit(submitData)
    } catch (err: any) {
      setError(err.message || 'Có lỗi xảy ra')
    }
  }

  const isDeadlinePassed = assignment?.deadline
    ? new Date(assignment.deadline) < new Date()
    : false
  const canEdit = review ? review.status === 'DRAFT' : true

  const getScoreLabel = (scoreValue: ReviewScore) => {
    const labels: Record<ReviewScore, string> = {
      STRONG_ACCEPT: t('review.scores.STRONG_ACCEPT'),
      ACCEPT: t('review.scores.ACCEPT'),
      WEAK_ACCEPT: t('review.scores.WEAK_ACCEPT'),
      BORDERLINE: t('review.scores.BORDERLINE'),
      WEAK_REJECT: t('review.scores.WEAK_REJECT'),
      REJECT: t('review.scores.REJECT'),
      STRONG_REJECT: t('review.scores.STRONG_REJECT'),
    }
    return labels[scoreValue]
  }

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
              {assignment.deadline
                ? new Date(assignment.deadline).toLocaleString('vi-VN')
                : 'Chưa thiết lập'}
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
            <CFormLabel>
              Tóm tắt <span className="text-danger">*</span>
            </CFormLabel>
            <CFormTextarea
              value={summary}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setSummary(e.target.value)}
              required
              rows={3}
              placeholder="Nhập tóm tắt đánh giá"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>
              Điểm đánh giá <span className="text-danger">*</span>
            </CFormLabel>
            <CFormSelect
              value={score}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setScore(e.target.value as ReviewScore)
              }
              required
              disabled={isDeadlinePassed || !canEdit}
            >
              <option value="STRONG_ACCEPT">{t('review.scores.STRONG_ACCEPT')}</option>
              <option value="ACCEPT">{t('review.scores.ACCEPT')}</option>
              <option value="WEAK_ACCEPT">{t('review.scores.WEAK_ACCEPT')}</option>
              <option value="BORDERLINE">{t('review.scores.BORDERLINE')}</option>
              <option value="WEAK_REJECT">{t('review.scores.WEAK_REJECT')}</option>
              <option value="REJECT">{t('review.scores.REJECT')}</option>
              <option value="STRONG_REJECT">{t('review.scores.STRONG_REJECT')}</option>
            </CFormSelect>
            <small className="text-muted">Điểm hiện tại: {getScoreLabel(score)}</small>
          </div>

          <div className="mb-3">
            <CFormLabel>
              Nhận xét <span className="text-danger">*</span>
            </CFormLabel>
            <CFormTextarea
              value={comments}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setComments(e.target.value)
              }
              required
              rows={5}
              placeholder="Nhập nhận xét về bài báo"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Điểm mạnh</CFormLabel>
            <CFormTextarea
              value={strengths}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setStrengths(e.target.value)
              }
              rows={3}
              placeholder="Nhập các điểm mạnh của bài báo"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Điểm yếu</CFormLabel>
            <CFormTextarea
              value={weaknesses}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setWeaknesses(e.target.value)
              }
              rows={3}
              placeholder="Nhập các điểm yếu của bài báo"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Đánh giá tổng thể (1-5)</CFormLabel>
            <CFormInput
              type="number"
              min="1"
              max="5"
              value={overallRating || ''}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setOverallRating(e.target.value ? parseInt(e.target.value) : undefined)
              }
              placeholder="Tùy chọn"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Độ tin cậy (1-5)</CFormLabel>
            <CFormInput
              type="number"
              min="1"
              max="5"
              value={confidence || ''}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setConfidence(e.target.value ? parseInt(e.target.value) : undefined)
              }
              placeholder="Tùy chọn"
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="mb-3">
            <CFormCheck
              type="checkbox"
              id="isConfidential"
              label="Đánh giá bảo mật (chỉ chair/PC thấy, author không thấy)"
              checked={isConfidential}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setIsConfidential(e.target.checked)
              }
              disabled={isDeadlinePassed || !canEdit}
            />
          </div>

          <div className="d-flex justify-content-between gap-2">
            <CButton color="secondary" onClick={onCancel} disabled={loading}>
              Hủy
            </CButton>
            <div className="d-flex gap-2">
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
              {review?.id && canEdit && (
                <CButton
                  color="success"
                  onClick={async () => {
                    if (
                      window.confirm(
                        'Bạn có chắc chắn muốn nộp (submit) review này? Sau khi nộp, bạn không thể chỉnh sửa.',
                      )
                    ) {
                      try {
                        await reviewService.submitReview(review.id)
                        onCancel() // Điều hướng về trang danh sách
                      } catch (err: any) {
                        setError(err.response?.data?.message || 'Không thể nộp review')
                      }
                    }
                  }}
                  disabled={loading}
                >
                  Nộp Review (Finalize)
                </CButton>
              )}
            </div>
          </div>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default ReviewForm
