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
import { reviewService, DiscussionMessage } from '../../services/review.service'

/**
 * DiscussionThread Props
 */
interface DiscussionThreadProps {
  reviewId: number
}

/**
 * DiscussionThread - Component hiển thị internal discussion thread
 *
 * Features:
 * - Hiển thị messages
 * - Thêm message mới
 * - Internal discussion (chỉ PC members thấy)
 */
const DiscussionThread: React.FC<DiscussionThreadProps> = ({ reviewId }) => {
  const { t } = useTranslation()
  const [messages, setMessages] = useState<DiscussionMessage[]>([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)

  const loadMessages = useCallback(async () => {
    try {
      setLoading(true)
      const data = await reviewService.getDiscussion(reviewId)
      setMessages(data)
    } catch (error) {
      console.error('Error loading discussion:', error)
    } finally {
      setLoading(false)
    }
  }, [reviewId])

  useEffect(() => {
    void loadMessages()
  }, [loadMessages])

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newMessage.trim()) return

    try {
      setSending(true)
      await reviewService.addDiscussionMessage({
        reviewId,
        content: newMessage.trim(),
      })
      setNewMessage('')
      await loadMessages()
    } catch (error) {
      console.error('Error sending message:', error)
      alert('Không thể gửi message')
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
      </CCardHeader>
      <CCardBody>
        {messages.length === 0 ? (
          <CAlert color="info">Chưa có thảo luận nào</CAlert>
        ) : (
          <div className="space-y-3 mb-4">
            {messages.map((message) => (
              <div key={message.id} className="border-bottom pb-3 mb-3">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <strong>{message.authorName}</strong>
                  <small className="text-muted">
                    {new Date(message.createdAt).toLocaleString('vi-VN')}
                  </small>
                </div>
                <p className="mb-0">{message.content}</p>
              </div>
            ))}
          </div>
        )}

        <CForm onSubmit={handleSendMessage}>
          <CFormTextarea
            value={newMessage}
            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setNewMessage(e.target.value)}
            placeholder="Nhập message..."
            rows={3}
            className="mb-2"
          />
          <CButton type="submit" color="primary" disabled={sending || !newMessage.trim()}>
            {sending ? <CSpinner size="sm" /> : 'Gửi'}
          </CButton>
        </CForm>
      </CCardBody>
    </CCard>
  )
}

export default DiscussionThread
