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
import { decisionService, Decision, CreateDecisionRequest } from '../../services/decision.service'

/**
 * DecisionBoard - Trang quản lý quyết định
 *
 * Features:
 * - Xem danh sách submissions cần quyết định
 * - Accept/Reject submissions
 * - Bulk decisions
 * - Xem review summary
 */
const DecisionBoard: React.FC = () => {
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [pendingDecisions, setPendingDecisions] = useState<Decision[]>([])
  const [loading, setLoading] = useState(true)
  const [showDecisionModal, setShowDecisionModal] = useState(false)
  const [selectedSubmission, setSelectedSubmission] = useState<Decision | null>(null)
  const [decision, setDecision] = useState<
    'ACCEPT' | 'REJECT' | 'MINOR_REVISION' | 'MAJOR_REVISION'
  >('ACCEPT')
  const [comments, setComments] = useState('')
  const [saving, setSaving] = useState(false)
  const [selectedIds, setSelectedIds] = useState<number[]>([])
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadPendingDecisions()
    }
  }, [conferenceId])

  const loadPendingDecisions = async () => {
    try {
      setLoading(true)
      const data = await decisionService.getPendingDecisions(conferenceId!)
      setPendingDecisions(data)
    } catch (error) {
      console.error('Error loading pending decisions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleMakeDecision = async () => {
    if (!selectedSubmission) return

    try {
      setSaving(true)
      setError('')
      await decisionService.createDecision({
        submissionId: selectedSubmission.submissionId,
        decision,
        comments: comments.trim() || undefined,
      })
      setSuccess('Đã tạo quyết định')
      setShowDecisionModal(false)
      setSelectedSubmission(null)
      setComments('')
      await loadPendingDecisions()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể tạo quyết định')
    } finally {
      setSaving(false)
    }
  }

  const handleBulkDecision = async () => {
    if (selectedIds.length === 0) {
      setError('Vui lòng chọn ít nhất một submission')
      return
    }

    if (!window.confirm(`Bạn có chắc chắn muốn quyết định ${selectedIds.length} submissions?`)) {
      return
    }

    try {
      setSaving(true)
      setError('')
      await decisionService.bulkDecisions({
        submissionIds: selectedIds,
        decision,
        comments: comments.trim() || undefined,
      })
      setSuccess(`Đã tạo quyết định cho ${selectedIds.length} submissions`)
      setSelectedIds([])
      setComments('')
      await loadPendingDecisions()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể tạo bulk decisions')
    } finally {
      setSaving(false)
    }
  }

  const getDecisionBadge = (decision: Decision['decision']) => {
    const colorMap: Record<string, string> = {
      ACCEPT: 'success',
      REJECT: 'danger',
      MINOR_REVISION: 'warning',
      MAJOR_REVISION: 'warning',
    }
    return <CBadge color={colorMap[decision] || 'secondary'}>{decision}</CBadge>
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
        <div className="d-flex justify-content-between align-items-center">
          <h4>Decision Board</h4>
          {selectedIds.length > 0 && (
            <div className="d-flex gap-2">
              <select
                className="form-select"
                value={decision}
                onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                  setDecision(e.target.value as any)
                }
                style={{ width: 'auto' }}
              >
                <option value="ACCEPT">Accept</option>
                <option value="MINOR_REVISION">Minor Revision</option>
                <option value="MAJOR_REVISION">Major Revision</option>
                <option value="REJECT">Reject</option>
              </select>
              <CButton color="primary" onClick={handleBulkDecision} disabled={saving}>
                Bulk Decision ({selectedIds.length})
              </CButton>
            </div>
          )}
        </div>
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

        {pendingDecisions.length === 0 ? (
          <p className="text-muted">Không có submission nào cần quyết định</p>
        ) : (
          <CTable hover>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>
                  <input
                    type="checkbox"
                    checked={selectedIds.length === pendingDecisions.length}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                      if (e.target.checked) {
                        setSelectedIds(pendingDecisions.map((d) => d.submissionId))
                      } else {
                        setSelectedIds([])
                      }
                    }}
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
              {pendingDecisions.map((item) => (
                <CTableRow key={item.submissionId}>
                  <CTableDataCell>
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(item.submissionId)}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        if (e.target.checked) {
                          setSelectedIds([...selectedIds, item.submissionId])
                        } else {
                          setSelectedIds(selectedIds.filter((id) => id !== item.submissionId))
                        }
                      }}
                    />
                  </CTableDataCell>
                  <CTableDataCell>{item.submissionId}</CTableDataCell>
                  <CTableDataCell>{item.submissionTitle}</CTableDataCell>
                  <CTableDataCell>{item.reviewCount}</CTableDataCell>
                  <CTableDataCell>{item.averageRating.toFixed(2)}</CTableDataCell>
                  <CTableDataCell>
                    <CButton
                      color="link"
                      size="sm"
                      onClick={() => {
                        setSelectedSubmission(item)
                        setShowDecisionModal(true)
                      }}
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

      {/* Decision Modal */}
      <CModal visible={showDecisionModal} onClose={() => setShowDecisionModal(false)}>
        <CModalHeader>
          <CModalTitle>Quyết định</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {selectedSubmission && (
            <>
              <div className="mb-3">
                <strong>Bài báo: </strong>
                {selectedSubmission.submissionTitle}
              </div>
              <div className="mb-3">
                <strong>Số reviews: </strong>
                {selectedSubmission.reviewCount}
              </div>
              <div className="mb-3">
                <strong>Điểm trung bình: </strong>
                {selectedSubmission.averageRating.toFixed(2)}
              </div>
              <div className="mb-3">
                <CFormLabel>Quyết định *</CFormLabel>
                <select
                  className="form-select"
                  value={decision}
                  onChange={(e: React.ChangeEvent<HTMLSelectElement>) =>
                    setDecision(e.target.value as any)
                  }
                >
                  <option value="ACCEPT">Chấp nhận</option>
                  <option value="MINOR_REVISION">Sửa đổi nhỏ</option>
                  <option value="MAJOR_REVISION">Sửa đổi lớn</option>
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
            </>
          )}
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
    </CCard>
  )
}

export default DecisionBoard
