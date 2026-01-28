import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
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
  CListGroup,
  CListGroupItem,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilCommentSquare, cilClipboard } from '@coreui/icons'
import {
  decisionService,
  Decision,
  DecisionType,
  CreateDecisionRequest,
  UpdateDecisionRequest,
  BulkDecisionRequest,
  DecisionHistory,
} from '../../services/decision.service'
import { conferenceService, ConferenceResponse } from '../../services/conference.service'

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
  const navigate = useNavigate()
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
  // Bulk states
  const [selectedSubmissionIds, setSelectedSubmissionIds] = useState<number[]>([])
  const [showBulkModal, setShowBulkModal] = useState(false)
  // History states
  const [showHistoryModal, setShowHistoryModal] = useState(false)
  const [historyList, setHistoryList] = useState<DecisionHistory[]>([])
  const [loadingHistory, setLoadingHistory] = useState(false)

  // Conference selection states
  const [myConferences, setMyConferences] = useState<ConferenceResponse[]>([])
  const [isSelectingConference, setIsSelectingConference] = useState(false)

  useEffect(() => {
    if (conferenceId) {
      loadDecisions()
    } else {
      loadMyConferences()
    }
  }, [conferenceId])

  const loadMyConferences = async () => {
    try {
      setLoading(true)
      const data = await conferenceService.getMyConferences()
      if (data.length === 1) {
        // Auto select if only one
        navigate(`?conferenceId=${data[0].id}`, { replace: true })
      } else {
        setMyConferences(data)
        setIsSelectingConference(true)
        setLoading(false)
      }
    } catch (error) {
      console.error('Error loading conferences:', error)
      setError('Không thể tải danh sách hội nghị')
      setLoading(false)
    }
  }

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

  const handleSelectAll = (pendingList: Decision[]) => {
    if (selectedSubmissionIds.length === pendingList.length) {
      setSelectedSubmissionIds([])
    } else {
      setSelectedSubmissionIds(pendingList.map(item => item.submissionId))
    }
  }

  const handleSelectOne = (submissionId: number) => {
    if (selectedSubmissionIds.includes(submissionId)) {
      setSelectedSubmissionIds(selectedSubmissionIds.filter(id => id !== submissionId))
    } else {
      setSelectedSubmissionIds([...selectedSubmissionIds, submissionId])
    }
  }

  const handleMakeBulkDecision = async () => {
    if (selectedSubmissionIds.length === 0) return

    try {
      setSaving(true)
      setError('')
      const request: BulkDecisionRequest = {
        submissionIds: selectedSubmissionIds,
        type: decisionType,
        comments: comments.trim() || undefined,
        sendNotification,
      }
      await decisionService.createBulkDecisions(request)
      setSuccess(`Đã tạo quyết định cho ${selectedSubmissionIds.length} bài báo thành công`)
      setShowBulkModal(false)
      setSelectedSubmissionIds([])
      setComments('')
      await loadDecisions()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể tạo quyết định hàng loạt')
    } finally {
      setSaving(false)
    }
  }

  const handleViewHistory = async (decisionId: number) => {
    try {
      setLoadingHistory(true)
      setShowHistoryModal(true)
      const logs = await decisionService.getDecisionHistory(decisionId)
      setHistoryList(logs)
    } catch (error: any) {
      setError('Không thể tải lịch sử quyết định')
    } finally {
      setLoadingHistory(false)
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
    if (loading) {
      return (
        <div className="d-flex justify-content-center p-5">
          <CSpinner color="primary" />
        </div>
      )
    }

    if (isSelectingConference) {
      return (
        <CCard className="mx-auto" style={{ maxWidth: '800px' }}>
          <CCardHeader>
            <h4>Chọn hội nghị để quản lý quyết định</h4>
          </CCardHeader>
          <CCardBody>
            {myConferences.length === 0 ? (
              <CAlert color="warning">Bạn chưa được gán vào hội nghị nào.</CAlert>
            ) : (
              <CListGroup>
                {myConferences.map(conf => (
                  <CListGroupItem
                    key={conf.id}
                    as="button"
                    onClick={() => navigate(`?conferenceId=${conf.id}`)}
                    className="d-flex justify-content-between align-items-center list-group-item-action"
                  >
                    <span>{conf.name}</span>
                    <CBadge color="primary" shape="rounded-pill">ID: {conf.id}</CBadge>
                  </CListGroupItem>
                ))}
              </CListGroup>
            )}
          </CCardBody>
        </CCard>
      )
    }

    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">Thiếu conferenceId và không thể tải danh sách hội nghị.</CAlert>
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
          <div className="d-flex justify-content-between align-items-center">
            <h4>Submissions cần quyết định</h4>
            {selectedSubmissionIds.length > 0 && (
              <CButton color="primary" onClick={() => setShowBulkModal(true)}>
                Quyết định hàng loạt ({selectedSubmissionIds.length})
              </CButton>
            )}
            <CButton color="secondary" variant="outline" size="sm" onClick={() => navigate('/app/chair/conferences')}>
              Đổi hội nghị
            </CButton>
          </div>
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
                  <CTableHeaderCell>
                    <CFormCheck
                      checked={selectedSubmissionIds.length === pendingSubmissions.length && pendingSubmissions.length > 0}
                      onChange={() => handleSelectAll(pendingSubmissions)}
                    />
                  </CTableHeaderCell>
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
                    <CTableDataCell>
                      <CFormCheck
                        checked={selectedSubmissionIds.includes(item.submissionId)}
                        onChange={() => handleSelectOne(item.submissionId)}
                      />
                    </CTableDataCell>
                    <CTableDataCell>{item.submissionId}</CTableDataCell>
                    <CTableDataCell>{item.submissionTitle}</CTableDataCell>
                    <CTableDataCell>{item.reviewSummary?.reviewCount || 0}</CTableDataCell>
                    <CTableDataCell>
                      {item.reviewSummary?.averageScore?.toFixed(2) || 'N/A'}
                    </CTableDataCell>
                    <CTableDataCell>
                      <div className="d-flex gap-2">
                        <CButton
                          color="info"
                          size="sm"
                          variant="outline"
                          title="Xem Reviews"
                          onClick={() => navigate(`/app/submissions/${item.submissionId}/reviews`)}
                        >
                          <CIcon icon={cilClipboard} />
                        </CButton>
                        <CButton
                          color="warning"
                          size="sm"
                          variant="outline"
                          title="Thảo luận"
                          onClick={() => navigate(`/app/chair/submissions/${item.submissionId}/discussion`)}
                        >
                          <CIcon icon={cilCommentSquare} />
                        </CButton>
                        <CButton
                          color="primary"
                          size="sm"
                          onClick={() => handleOpenDecisionModal(item.submissionId)}
                        >
                          Quyết định
                        </CButton>
                      </div>
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
                        <CButton
                          color="secondary"
                          size="sm"
                          onClick={() => handleViewHistory(item.id)}
                        >
                          Lịch sử
                        </CButton>
                      </div>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      {/* Bulk Decision Modal */}
      <CModal visible={showBulkModal} onClose={() => setShowBulkModal(false)} size="lg">
        <CModalHeader>
          <CModalTitle>Quyết định hàng loạt ({selectedSubmissionIds.length} bài)</CModalTitle>
        </CModalHeader>
        <CModalBody>
          <CAlert color="info">
            Bạn đang ra quyết định cho {selectedSubmissionIds.length} bài báo đã chọn.
          </CAlert>
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
            <CFormLabel>Nhận xét chung (tùy chọn)</CFormLabel>
            <CFormTextarea
              value={comments}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
                setComments(e.target.value)
              }
              rows={5}
              placeholder="Nhập nhận xét chung cho các bài báo này"
            />
          </div>
          <div className="mb-3">
            <CFormCheck
              id="sendBulkNotification"
              label="Gửi email thông báo ngay cho tất cả"
              checked={sendNotification}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setSendNotification(e.target.checked)
              }
            />
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowBulkModal(false)}>
            Hủy
          </CButton>
          <CButton color="primary" onClick={handleMakeBulkDecision} disabled={saving}>
            {saving ? <CSpinner size="sm" /> : 'Lưu tất cả'}
          </CButton>
        </CModalFooter>
      </CModal>

      {/* History Modal */}
      <CModal visible={showHistoryModal} onClose={() => setShowHistoryModal(false)} size="lg">
        <CModalHeader>
          <CModalTitle>Lịch sử thay đổi quyết định</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {loadingHistory ? (
            <div className="text-center p-3">
              <CSpinner color="primary" />
            </div>
          ) : historyList.length === 0 ? (
            <p className="text-muted">Không có dữ liệu lịch sử</p>
          ) : (
            <CTable hover small responsive>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Thời gian</CTableHeaderCell>
                  <CTableHeaderCell>Hành động</CTableHeaderCell>
                  <CTableHeaderCell>Người thực hiện</CTableHeaderCell>
                  <CTableHeaderCell>Chi tiết</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {historyList.map((log) => (
                  <CTableRow key={log.id}>
                    <CTableDataCell>
                      <small>{new Date(log.changedAt).toLocaleString('vi-VN')}</small>
                    </CTableDataCell>
                    <CTableDataCell>
                      <CBadge color="info">{log.changeType}</CBadge>
                    </CTableDataCell>
                    <CTableDataCell>{log.changedByName}</CTableDataCell>
                    <CTableDataCell>
                      <div className="small">
                        {log.fieldName && <div><strong>Trường:</strong> {log.fieldName}</div>}
                        {log.oldValue && <div><strong>Cũ:</strong> {log.oldValue}</div>}
                        {log.newValue && <div><strong>Mới:</strong> {log.newValue}</div>}
                        {log.description && <div className="text-muted italic">{log.description}</div>}
                      </div>
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowHistoryModal(false)}>
            Đóng
          </CButton>
        </CModalFooter>
      </CModal>

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
