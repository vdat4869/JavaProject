import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormTextarea,
  CFormLabel,
  CButton,
  CAlert,
  CSpinner,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
} from '@coreui/react'
import {
  notificationService,
  EmailNotificationRequest,
  BulkEmailPreview,
} from '../../services/notification.service'
import { decisionService, Decision } from '../../services/decision.service'

/**
 * BulkEmailPreview - Trang preview và gửi bulk email
 *
 * Features:
 * - Preview bulk email
 * - Chọn recipients từ decisions
 * - Gửi bulk email notifications
 */
const BulkEmailPreview: React.FC = () => {
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [decisions, setDecisions] = useState<Decision[]>([])
  const [selectedRecipientIds, setSelectedRecipientIds] = useState<number[]>([])
  const [subject, setSubject] = useState('')
  const [templateName, setTemplateName] = useState('decision-accept')
  const [preview, setPreview] = useState<BulkEmailPreview | null>(null)
  const [loading, setLoading] = useState(true)
  const [previewing, setPreviewing] = useState(false)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadDecisions()
    }
  }, [conferenceId])

  const loadDecisions = async () => {
    try {
      setLoading(true)
      const data = await decisionService.getDecisions(conferenceId!)
      setDecisions(data)
    } catch (error) {
      console.error('Error loading decisions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handlePreview = async () => {
    if (selectedRecipientIds.length === 0) {
      setError('Vui lòng chọn ít nhất một recipient')
      return
    }

    if (!subject.trim()) {
      setError('Vui lòng nhập subject')
      return
    }

    try {
      setPreviewing(true)
      setError('')
      const previewData = await notificationService.previewBulkEmail({
        recipientIds: selectedRecipientIds,
        subject: subject.trim(),
        templateName,
        templateData: {
          conferenceId,
        },
      })
      setPreview(previewData)
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể preview email')
    } finally {
      setPreviewing(false)
    }
  }

  const handleSend = async () => {
    if (!preview) return

    if (!window.confirm(`Gửi email đến ${preview.recipientCount} recipients?`)) {
      return
    }

    try {
      setSending(true)
      setError('')
      await notificationService.sendBulkEmail({
        recipientIds: selectedRecipientIds,
        subject: subject.trim(),
        templateName,
        templateData: {
          conferenceId,
        },
      })
      setSuccess(`Đã gửi email đến ${preview.recipientCount} recipients`)
      setPreview(null)
      setSelectedRecipientIds([])
      setSubject('')
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể gửi email')
    } finally {
      setSending(false)
    }
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

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Bulk Email Preview</h4>
      </CCardHeader>
      <CCardBody>
        {error && (
          <CAlert color="danger" className="mb-3">
            {error}
          </CAlert>
        )}
        {success && (
          <CAlert color="success" className="mb-3">
            {success}
          </CAlert>
        )}

        <CForm>
          <div className="mb-3">
            <CFormLabel>Chọn recipients từ decisions</CFormLabel>
            <div
              style={{
                maxHeight: '200px',
                overflowY: 'auto',
                border: '1px solid #ddd',
                padding: '10px',
              }}
            >
              {decisions.map((decision) => (
                <div key={decision.id} className="mb-2">
                  <input
                    type="checkbox"
                    id={`recipient-${decision.id}`}
                    checked={selectedRecipientIds.includes(decision.id)}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      if (e.target.checked) {
                        setSelectedRecipientIds([...selectedRecipientIds, decision.id])
                      } else {
                        setSelectedRecipientIds(
                          selectedRecipientIds.filter((id) => id !== decision.id),
                        )
                      }
                    }}
                    className="form-check-input me-2"
                  />
                  <label htmlFor={`recipient-${decision.id}`} className="form-check-label">
                    {decision.submissionTitle} - {decision.decidedBy}
                  </label>
                </div>
              ))}
            </div>
            <small className="text-muted">Đã chọn: {selectedRecipientIds.length} recipients</small>
          </div>

          <div className="mb-3">
            <CFormLabel>Subject *</CFormLabel>
            <CFormInput
              type="text"
              value={subject}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSubject(e.target.value)}
              placeholder="Email subject"
              required
            />
          </div>

          <div className="mb-3">
            <CFormLabel>Template</CFormLabel>
            <select
              className="form-select"
              value={templateName}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setTemplateName(e.target.value)
              }
            >
              <option value="decision-accept">Decision Accept</option>
              <option value="decision-reject">Decision Reject</option>
              <option value="decision-revision">Decision Revision</option>
            </select>
          </div>

          <div className="mb-3">
            <CButton color="primary" onClick={handlePreview} disabled={previewing}>
              {previewing ? <CSpinner size="sm" /> : 'Preview'}
            </CButton>
          </div>
        </CForm>

        {preview && (
          <div className="mt-4">
            <h5>Preview</h5>
            <div className="mb-3">
              <strong>Recipients: </strong>
              {preview.recipientCount}
            </div>
            <div className="mb-3">
              <strong>Subject: </strong>
              {preview.subject}
            </div>
            <div className="mb-3">
              <strong>Preview: </strong>
              <div
                className="border p-3"
                style={{ maxHeight: '300px', overflowY: 'auto' }}
                dangerouslySetInnerHTML={{ __html: preview.preview }}
              />
            </div>
            <CButton color="success" onClick={handleSend} disabled={sending}>
              {sending ? <CSpinner size="sm" /> : `Gửi đến ${preview.recipientCount} recipients`}
            </CButton>
          </div>
        )}
      </CCardBody>
    </CCard>
  )
}

export default BulkEmailPreview
