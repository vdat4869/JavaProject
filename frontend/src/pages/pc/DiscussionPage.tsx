import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { CCard, CCardBody, CCardHeader, CSpinner, CButton, CAlert } from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { reviewService, Review, Rebuttal } from '../../services/review.service'
import { useAuth } from '../../context/AuthContext'
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
  const { id, submissionId } = useParams<{ id?: string, submissionId?: string }>()
  const navigate = useNavigate()
  const [review, setReview] = useState<Review | null>(null)
  const [rebuttal, setRebuttal] = useState<Rebuttal | null>(null)
  const [loading, setLoading] = useState(true)
  const [activeSubmissionId, setActiveSubmissionId] = useState<number | null>(null)
  const [allReviews, setAllReviews] = useState<Review[]>([])
  const { user } = useAuth()
  const isChairOrAdmin = user?.roles.includes('CHAIR') || user?.roles.includes('ADMIN')

  useEffect(() => {
    loadData()
  }, [id, submissionId, user]) // Added user to dependencies to react to role changes

  const loadData = async () => {
    try {
      setLoading(true)
      let currentSubmissionId: number | null = null
      let reviewData: Review | null = null

      if (id) {
        // Load by reviewId or assignmentId
        try {
          reviewData = await reviewService.getReview(parseInt(id))
          currentSubmissionId = reviewData.submissionId
        } catch (error) {
          // If reviewId fails, try to load by assignmentId
          try {
            reviewData = await reviewService.getReviewByAssignment(parseInt(id))
            if (reviewData) {
              currentSubmissionId = reviewData.submissionId
            } else {
              // If no review found by assignment, try to get assignment to at least get submissionId
              const assignment = await reviewService.getAssignment(parseInt(id))
              currentSubmissionId = assignment.submissionId
            }
          } catch (e) {
            console.error('Failed to load review or assignment by ID:', id, e)
          }
        }
      } else if (submissionId) {
        // Load by submissionId directly
        currentSubmissionId = parseInt(submissionId)

        // 1. Logic cho PC/Reviewer: Tìm bài review của mình
        if (!isChairOrAdmin) {
          try {
            const assignments = await reviewService.getAssignments()
            const myAssignment = assignments.find(a => a.submissionId === currentSubmissionId)
            if (myAssignment && myAssignment.reviewId) {
              reviewData = await reviewService.getReview(myAssignment.reviewId)
            } else if (myAssignment) { // If assignment exists but no reviewId, try getting review by assignment ID
              reviewData = await reviewService.getReviewByAssignment(myAssignment.id)
            }
          } catch (err) {
            // Ignore 403 or other errors when fetching assignments (e.g. Chair viewing discussion)
            console.log('Skipping my-review lookup (possibly Chair role or no assignment)', err)
          }
        }
      }

      setReview(reviewData)
      setActiveSubmissionId(currentSubmissionId)

      // 2. Logic cho Chair/Admin: Load TẤT CẢ reviews để tham khảo
      if (currentSubmissionId && isChairOrAdmin) {
        try {
          const reviews = await reviewService.getReviewsBySubmission(currentSubmissionId)
          setAllReviews(reviews)
        } catch (e) {
          console.error('Failed to load all reviews for chair', e)
          setAllReviews([]) // Ensure it's empty on error
        }
      } else {
        setAllReviews([]) // Clear allReviews if not chair or no submissionId
      }

      // Load rebuttal nếu có
      if (currentSubmissionId) {
        try {
          const rebuttalData = await reviewService.getRebuttal(currentSubmissionId)
          setRebuttal(rebuttalData)
        } catch (error) {
          // Rebuttal không tồn tại hoặc lỗi khác, không cần log nếu chỉ là 404
          setRebuttal(null)
        }
      } else {
        setRebuttal(null)
      }
    } catch (error) {
      console.error('Error loading data for discussion:', error)
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

  if (!activeSubmissionId) {
    return (
      <CCard>
        <CCardBody>
          <p className="text-danger">Không tìm thấy bài nộp hoặc bối cảnh thảo luận</p>
        </CCardBody>
      </CCard>
    )
  }

  const renderReviewInfo = () => {
    // Case 1: Chair/Admin xem danh sách tổng hợp
    if (isChairOrAdmin && allReviews.length > 0) {
      return (
        <div className="chair-reviews-view">
          <CAlert color="info" className="d-flex justify-content-between align-items-center">
            <span>
              <strong>Góc nhìn quản lý:</strong> Bạn đang xem tổng hợp {allReviews.length} bài đánh giá của các Reviewers.
            </span>
            <CButton
              size="sm"
              color="light"
              variant="outline"
              onClick={() => navigate(`/app/submissions/${activeSubmissionId}/reviews`)}
            >
              Xem chi tiết
            </CButton>
          </CAlert>
          <div className="row">
            {allReviews.map((r, idx) => (
              <div className="col-md-6 mb-3" key={r.id}>
                <CCard className={`h-100 ${r.score.includes('ACCEPT') ? 'border-success' : 'border-danger'}`}>
                  <CCardBody>
                    <h6 className="card-title d-flex justify-content-between">
                      <span>Reviewer #{idx + 1}</span>
                      <span className={`badge ${r.score.includes('ACCEPT') ? 'bg-success' : 'bg-danger'}`}>
                        {r.score}
                      </span>
                    </h6>
                    <p className="card-text small text-muted mb-2">
                      Score: {r.numericScore}/7 | Conf: {r.confidence}/5
                    </p>
                    <p className="card-text small mb-0 fst-italic">
                      "{r.summary ? r.summary.substring(0, 100) + '...' : 'Không có tóm tắt'}"
                    </p>
                  </CCardBody>
                </CCard>
              </div>
            ))}
          </div>
        </div>
      )
    }

    // Case 2: PC Member xem bài của mình
    if (!review) {
      return (
        <CAlert color="warning">
          Bạn chưa nộp bài đánh giá cho bài báo này hoặc chưa được phân công.
        </CAlert>
      )
    }

    return (
      <div className="review-info-summary">
        <div className="mb-2">
          <strong>Điểm đánh giá: </strong>
          {review.score}
          {review.numericScore && ` (${review.numericScore}/7)`}
        </div>
        {review.overallRating && (
          <div className="mb-2">
            <strong>Đánh giá tổng thể: </strong>
            {review.overallRating}/5
          </div>
        )}
        {review.confidence && (
          <div className="mb-2">
            <strong>Độ tin cậy: </strong>
            {review.confidence}/5
          </div>
        )}
        {review.summary && (
          <div className="mb-2">
            <strong>Tóm tắt: </strong>
            <p className="mb-0">{review.summary}</p>
          </div>
        )}
        {review.comments && (
          <div className="mb-2">
            <strong>Nhận xét: </strong>
            <p className="mb-0">{review.comments}</p>
          </div>
        )}
        {review.isConfidential && (
          <div className="mb-2">
            <span className="badge bg-warning">Đánh giá bảo mật</span>
          </div>
        )}
      </div>
    )
  }

  return (
    <>
      <CCard className="mb-3">
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Thảo luận đánh giá</h4>
            <CButton color="secondary" onClick={() => navigate(-1)}>
              Quay lại
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          {renderReviewInfo()}
        </CCardBody>
      </CCard>

      {/* Rebuttal View */}
      {rebuttal && (
        <CCard className="mb-3">
          <CCardHeader>
            <h5>Rebuttal từ Author</h5>
            {rebuttal.status === 'DRAFT' && (
              <small className="text-muted">(Bản nháp)</small>
            )}
          </CCardHeader>
          <CCardBody>
            <p>{rebuttal.content}</p>
            {rebuttal.submittedAt && (
              <small className="text-muted">
                Gửi vào: {new Date(rebuttal.submittedAt).toLocaleString('vi-VN')}
              </small>
            )}
          </CCardBody>
        </CCard>
      )}

      <DiscussionThread submissionId={activeSubmissionId} />
    </>
  )
}

export default DiscussionPage
