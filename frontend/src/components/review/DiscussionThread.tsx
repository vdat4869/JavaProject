import React, { useState, useEffect, useCallback } from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormTextarea,
  CButton,
  CSpinner,
  CAlert,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { reviewService, ReviewComment } from '../../services/review.service'

/**
 * DiscussionThread Props
 */
interface DiscussionThreadProps {
  submissionId: number
}

/**
 * DiscussionThread - Component hiển thị internal discussion thread
 *
 * Features:
 * - Hiển thị internal comments
 * - Thêm comment mới
 * - Internal discussion (chỉ PC members/chair/admin thấy)
 */
const DiscussionThread: React.FC<DiscussionThreadProps> = ({ submissionId }) => {
  const { t } = useTranslation()
  const [comments, setComments] = useState<ReviewComment[]>([])
  const [newComment, setNewComment] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)

  const loadComments = useCallback(async () => {
    try {
      setLoading(true)
      const data = await reviewService.getInternalComments(submissionId)
      setComments(data)
    } catch (error) {
      console.error('Error loading comments:', error)
    } finally {
      setLoading(false)
    }
  }, [submissionId])

  useEffect(() => {
    void loadComments()
  }, [loadComments])

  const handleSendComment = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newComment.trim()) return

    try {
      setSending(true)
      await reviewService.addInternalComment(submissionId, newComment.trim())
      setNewComment('')
      await loadComments()
    } catch (error: any) {
      console.error('Error sending comment:', error)
      alert(error.response?.data?.message || 'Không thể gửi comment')
    } finally {
      setSending(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-3">
        <CSpinner color="primary" size="sm" />
      </div>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h5>Thảo luận nội bộ</h5>
        <small className="text-muted">
          Chỉ PC members, chair và admin có thể xem và thêm comments
        </small>
      </CCardHeader>
      <CCardBody>
        {comments.length === 0 ? (
          <CAlert color="info">Chưa có thảo luận nào</CAlert>
        ) : (
          <div className="space-y-3 mb-4">
            {comments.map((comment) => (
              <div key={comment.id} className="border-bottom pb-3 mb-3">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <strong>
                    {comment.reviewerName || `Reviewer #${comment.reviewerId}`}
                  </strong>
                  <small className="text-muted">
                    {new Date(comment.createdAt).toLocaleString('vi-VN')}
                  </small>
                </div>
                <p className="mb-0">{comment.content}</p>
              </div>
            ))}
          </div>
        )}

        <CForm onSubmit={handleSendComment}>
          <CFormTextarea
            value={newComment}
            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
              setNewComment(e.target.value)
            }
            placeholder="Nhập comment nội bộ..."
            rows={3}
            className="mb-2"
          />
          <CButton type="submit" color="primary" disabled={sending || !newComment.trim()}>
            {sending ? <CSpinner size="sm" /> : 'Gửi'}
          </CButton>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default DiscussionThread
