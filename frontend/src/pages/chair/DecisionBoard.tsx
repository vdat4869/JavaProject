import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CButton,
  CSpinner,
  CAlert,
  CBadge,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CFormTextarea,
  CFormLabel,
  CFormCheck,
} from '@coreui/react'
import {
  decisionService,
  Decision,
  DecisionType,
  CreateDecisionRequest,
  UpdateDecisionRequest,
} from '../../services/decision.service'

/**
 * DecisionBoard - Trang quản lý quyết định
 *
 * Features:
 * - Xem danh sách submissions cần quyết định
 * - Accept/Reject submissions
 * - Gửi notifications
 * - Xem review summary
 */
const DecisionBoard: React.FC = () => {
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [decisions, setDecisions] = useState<Decision[]>([])
  const [loading, setLoading] = useState(true)
  const [showDecisionModal, setShowDecisionModal] = useState(false)
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<number | null>(null)
  const [decisionType, setDecisionType] = useState<DecisionType>('ACCEPT')
  const [comments, setComments] = useState('')
  const [sendNotification, setSendNotification] = useState(true)
  const [saving, setSaving] = useState(false)
  const [sendingNotification, setSendingNotification] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  // Edit decision states
  const [showEditModal, setShowEditModal] = useState(false)
  const [editingDecision, setEditingDecision] = useState<Decision | null>(null)
  const [editType, setEditType] = useState<DecisionType>('ACCEPT')
  const [editComments, setEditComments] = useState('')
  const [editReason, setEditReason] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadDecisions()
    }
  }, [conferenceId])

  const loadDecisions = async () => {
    try {
      setLoading(true)
      const data = await decisionService.getDecisionsByConference(conferenceId!)
      setDecisions(data)
    } catch (error) {
      console.error('Error loading decisions:', error)
      setError('Không thể tải danh sách quyết định')
    } finally {
      setLoading(false)
    }
  }

  const handleOpenDecisionModal = (submissionId: number) => {
    setSelectedSubmissionId(submissionId)
    setDecisionType('ACCEPT')
    setComments('')
    setSendNotification(true)
    setShowDecisionModal(true)
  }

  const handleMakeDecision = async () => {
    if (!selectedSubmissionId) return

    try {
      setSaving(true)
      setError('')
      const request: CreateDecisionRequest = {
        submissionId: selectedSubmissionId,
        type: decisionType,
        comments: comments.trim() || undefined,
        sendNotification,
      }
      await decisionService.createDecision(request)
      setSuccess('Đã tạo quyết định thành công')
      setShowDecisionModal(false)
      setSelectedSubmissionId(null)
      setComments('')
      await loadDecisions()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể tạo quyết định')
    } finally {
      setSaving(false)
    }
  }

  const handleSendNotification = async (decisionId: number) => {
    try {
      setSendingNotification(decisionId)
      setError('')
      await decisionService.sendNotification(decisionId)
      setSuccess('Đã gửi notification thành công')
      await loadDecisions()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể gửi notification')
    } finally {
      setSendingNotification(null)
    }
  }

  const handleOpenEditModal = (decision: Decision) => {
    setEditingDecision(decision)
    setEditType(decision.type)
    setEditComments(decision.comments || '')
    setEditReason('')
    setShowEditModal(true)
  }

  const handleUpdateDecision = async () => {
    if (!editingDecision || !editReason.trim()) {
      setError('Vui lòng nhập lý do thay đổi')
      return
    }

    try {
      setSaving(true)
      setError('')
      const request: UpdateDecisionRequest = {
        type: editType !== editingDecision.type ? editType : undefined,
        comments: editComments !== editingDecision.comments ? editComments : undefined,
        reason: editReason.trim(),
      }
      await decisionService.updateDecision(editingDecision.id, request)
      setSuccess('Đã cập nhật quyết định thành công')
      setShowEditModal(false)
      setEditingDecision(null)
      setEditReason('')
      await loadDecisions()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể cập nhật quyết định')
    } finally {
      setSaving(false)
    }
  }

  const getDecisionBadge = (type: DecisionType) => {
    const colorMap: Record<DecisionType, string> = {
      ACCEPT: 'success',
      REJECT: 'danger',
      CONDITIONAL_ACCEPT: 'warning',
    }
    const labelMap: Record<DecisionType, string> = {
      ACCEPT: 'Chấp nhận',
      REJECT: 'Từ chối',
      CONDITIONAL_ACCEPT: 'Chấp nhận có điều kiện',
    }
    return <CBadge color={colorMap[type]}>{labelMap[type]}</CBadge>
  }

  if (!conferenceId) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Thiếu conferenceId</CAlert>
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

  // Lọc submissions chưa có decision (pending)
  const pendingSubmissions = decisions.filter((d) => !d.type)
  // Submissions đã có decision
  const decidedSubmissions = decisions.filter((d) => d.type)

  return (
    <>
      <CCard className="mb-4">
        <CCardHeader>
          <h4>Submissions cần quyết định</h4>
        </CCardHeader>
        <CCardBody>
          {error && (
            <CAlert color="danger" className="mb-3" dismissible onClose={() => setError('')}>
              {error}
            </CAlert>
          )}
          {success && (
            <CAlert color="success" className="mb-3" dismissible onClose={() => setSuccess('')}>
              {success}
            </CAlert>
          )}

          {pendingSubmissions.length === 0 ? (
            <p className="text-muted">Tất cả submissions đã được quyết định</p>
          ) : (
            <CTable hover>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>ID</CTableHeaderCell>
                  <CTableHeaderCell>Tiêu đề</CTableHeaderCell>
                  <CTableHeaderCell>Số reviews</CTableHeaderCell>
                  <CTableHeaderCell>Điểm TB</CTableHeaderCell>
                  <CTableHeaderCell>Thao tác</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {pendingSubmissions.map((item) => (
                  <CTableRow key={item.submissionId}>
                    <CTableDataCell>{item.submissionId}</CTableDataCell>
                    <CTableDataCell>{item.submissionTitle}</CTableDataCell>
                    <CTableDataCell>{item.reviewSummary?.reviewCount || 0}</CTableDataCell>
                    <CTableDataCell>
                      {item.reviewSummary?.averageScore?.toFixed(2) || 'N/A'}
                    </CTableDataCell>
                    <CTableDataCell>
                      <CButton
                        color="primary"
                        size="sm"
                        onClick={() => handleOpenDecisionModal(item.submissionId)}
                      >
                        Quyết định
                      </CButton>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>
          <h4>Đã quyết định</h4>
        </CCardHeader>
        <CCardBody>
          {decidedSubmissions.length === 0 ? (
            <p className="text-muted">Chưa có quyết định nào</p>
          ) : (
            <CTable hover>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>ID</CTableHeaderCell>
                  <CTableHeaderCell>Tiêu đề</CTableHeaderCell>
                  <CTableHeaderCell>Quyết định</CTableHeaderCell>
                  <CTableHeaderCell>Ngày quyết định</CTableHeaderCell>
                  <CTableHeaderCell>Thông báo</CTableHeaderCell>
                  <CTableHeaderCell>Thao tác</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {decidedSubmissions.map((item) => (
                  <CTableRow key={item.id}>
                    <CTableDataCell>{item.submissionId}</CTableDataCell>
                    <CTableDataCell>{item.submissionTitle}</CTableDataCell>
                    <CTableDataCell>{getDecisionBadge(item.type)}</CTableDataCell>
                    <CTableDataCell>
                      {new Date(item.decidedAt).toLocaleDateString('vi-VN')}
                    </CTableDataCell>
                    <CTableDataCell>
                      {item.notified ? (
                        <CBadge color="success">Đã gửi</CBadge>
                      ) : (
                        <CBadge color="secondary">Chưa gửi</CBadge>
                      )}
                      {item.locked && (
                        <CBadge color="dark" className="ms-1">
                          Đã khóa
                        </CBadge>
                      )}
                    </CTableDataCell>
                    <CTableDataCell>
                      <div className="d-flex gap-1">
                        {!item.notified && !item.locked && (
                          <CButton
                            color="warning"
                            size="sm"
                            onClick={() => handleOpenEditModal(item)}
                          >
                            Sửa
                          </CButton>
                        )}
                        {!item.notified && (
                          <CButton
                            color="info"
                            size="sm"
                            onClick={() => handleSendNotification(item.id)}
                            disabled={sendingNotification === item.id}
                          >
                            {sendingNotification === item.id ? (
                              <CSpinner size="sm" />
                            ) : (
                              'Gửi thông báo'
                            )}
                          </CButton>
                        )}
                      </div>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      {/* Decision Modal */}
      <CModal visible={showDecisionModal} onClose={() => setShowDecisionModal(false)}>
        <CModalHeader>
          <CModalTitle>Tạo quyết định</CModalTitle>
        </CModalHeader>
        <CModalBody>
          <div className="mb-3">
            <CFormLabel>Loại quyết định *</CFormLabel>
            <select
              className="form-select"
              value={decisionType}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                setDecisionType(e.target.value as DecisionType)
              }
            >
              <option value="ACCEPT">Chấp nhận</option>
              <option value="CONDITIONAL_ACCEPT">Chấp nhận có điều kiện</option>
              <option value="REJECT">Từ chối</option>
            </select>
          </div>
          <div className="mb-3">
            <CFormLabel>Nhận xét (tùy chọn)</CFormLabel>
            <CFormTextarea
              value={comments}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setComments(e.target.value)
              }
              rows={5}
              placeholder="Nhập nhận xét về quyết định"
            />
          </div>
          <div className="mb-3">
            <CFormCheck
              id="sendNotification"
              label="Gửi email thông báo ngay"
              checked={sendNotification}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setSendNotification(e.target.checked)
              }
            />
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowDecisionModal(false)}>
            Hủy
          </CButton>
          <CButton color="primary" onClick={handleMakeDecision} disabled={saving}>
            {saving ? <CSpinner size="sm" /> : 'Lưu'}
          </CButton>
        </CModalFooter>
      </CModal>

      {/* Edit Decision Modal */}
      <CModal visible={showEditModal} onClose={() => setShowEditModal(false)}>
        <CModalHeader>
          <CModalTitle>Sửa quyết định</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {editingDecision && (
            <>
              <div className="mb-3">
                <strong>Bài báo: </strong>
                {editingDecision.submissionTitle}
              </div>
              <div className="mb-3">
                <CFormLabel>Loại quyết định *</CFormLabel>
                <select
                  className="form-select"
                  value={editType}
                  onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                    setEditType(e.target.value as DecisionType)
                  }
                >
                  <option value="ACCEPT">Chấp nhận</option>
                  <option value="CONDITIONAL_ACCEPT">Chấp nhận có điều kiện</option>
                  <option value="REJECT">Từ chối</option>
                </select>
              </div>
              <div className="mb-3">
                <CFormLabel>Nhận xét</CFormLabel>
                <CFormTextarea
                  value={editComments}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                    setEditComments(e.target.value)
                  }
                  rows={4}
                  placeholder="Nhập nhận xét về quyết định"
                />
              </div>
              <div className="mb-3">
                <CFormLabel>Lý do thay đổi *</CFormLabel>
                <CFormTextarea
                  value={editReason}
                  onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                    setEditReason(e.target.value)
                  }
                  rows={2}
                  placeholder="Nhập lý do thay đổi quyết định (bắt buộc)"
                />
              </div>
            </>
          )}
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowEditModal(false)}>
            Hủy
          </CButton>
          <CButton color="primary" onClick={handleUpdateDecision} disabled={saving || !editReason.trim()}>
            {saving ? <CSpinner size="sm" /> : 'Cập nhật'}
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default DecisionBoard
