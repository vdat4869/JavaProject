import React, { useState, useEffect } from 'react'
import {
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CButton,
  CFormLabel,
  CFormSelect,
  CFormTextarea,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { assignmentService, ReassignRequestDTO, Assignment } from '../../services/assignment.service'
import { pcService, PCMember } from '../../services/pc.service'
import { pcService as pcServiceForWorkload, Workload } from '../../services/pc.service'

interface ReassignmentModalProps {
  visible: boolean
  onClose: () => void
  assignment: Assignment | null
  conferenceId: number
  onSuccess: () => void
}

/**
 * ReassignmentModal - Modal để reassign assignment sang reviewer khác
 */
const ReassignmentModal: React.FC<ReassignmentModalProps> = ({
  visible,
  onClose,
  assignment,
  conferenceId,
  onSuccess,
}) => {
  const [pcMembers, setPCMembers] = useState<PCMember[]>([])
  const [selectedReviewerId, setSelectedReviewerId] = useState<number | null>(null)
  const [reason, setReason] = useState('')
  const [loading, setLoading] = useState(false)
  const [loadingMembers, setLoadingMembers] = useState(false)
  const [error, setError] = useState('')
  const [workloads, setWorkloads] = useState<Record<number, Workload>>({})

  useEffect(() => {
    if (visible && conferenceId && assignment) {
      loadPCMembers()
    }
  }, [visible, conferenceId, assignment])

  const loadPCMembers = async () => {
    try {
      setLoadingMembers(true)
      const members = await pcService.getPCMembers(conferenceId)
      // Only show accepted PC members, exclude current reviewer
      const acceptedMembers = members.filter(
        (m) => m.status === 'ACCEPTED' && m.userId !== assignment?.reviewerId
      )
      setPCMembers(acceptedMembers)

      // Load workloads for all members
      const workloadPromises = acceptedMembers.map(async (member) => {
        try {
          const workload = await pcServiceForWorkload.getReviewerWorkload(
            member.userId,
            conferenceId
          )
          return { userId: member.userId, workload }
        } catch (error) {
          return { userId: member.userId, workload: null }
        }
      })

      const workloadResults = await Promise.all(workloadPromises)
      const workloadMap: Record<number, Workload> = {}
      workloadResults.forEach((result) => {
        if (result.workload) {
          workloadMap[result.userId] = result.workload
        }
      })
      setWorkloads(workloadMap)
    } catch (error: any) {
      setError('Không thể tải danh sách PC members')
    } finally {
      setLoadingMembers(false)
    }
  }

  const handleSubmit = async () => {
    if (!selectedReviewerId || !assignment) {
      setError('Vui lòng chọn reviewer mới')
      return
    }

    try {
      setLoading(true)
      setError('')
      const data: ReassignRequestDTO = {
        newReviewerId: selectedReviewerId,
        reason: reason || undefined,
      }
      await assignmentService.reassignAssignment(assignment.id, data)
      onSuccess()
      handleClose()
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          error.response?.data?.error ||
          'Không thể reassign assignment'
      )
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setSelectedReviewerId(null)
    setReason('')
    setError('')
    onClose()
  }

  const getWorkloadStatus = (userId: number) => {
    const workload = workloads[userId]
    if (!workload) return null
    return workload.workloadStatus
  }

  const getWorkloadBadge = (status: string | null) => {
    if (!status) return null
    const colorMap: Record<string, string> = {
      LOW: 'success',
      NORMAL: 'info',
      HIGH: 'warning',
      OVERLOADED: 'danger',
    }
    const labelMap: Record<string, string> = {
      LOW: 'Thấp',
      NORMAL: 'Bình thường',
      HIGH: 'Cao',
      OVERLOADED: 'Quá tải',
    }
    return (
      <span className={`badge bg-${colorMap[status] || 'secondary'} ms-2`}>
        {labelMap[status] || status}
      </span>
    )
  }

  if (!assignment) {
    return null
  }

  return (
    <CModal visible={visible} onClose={handleClose} size="lg">
      <CModalHeader>
        <CModalTitle>Reassign Assignment</CModalTitle>
      </CModalHeader>
      <CModalBody>
        <CAlert color="info" className="mb-3">
          <strong>Assignment hiện tại:</strong>
          <br />
          Submission: {assignment.submissionTitle}
          <br />
          Reviewer hiện tại: {assignment.reviewerName} ({assignment.reviewerEmail})
        </CAlert>

        {error && (
          <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
            {error}
          </CAlert>
        )}

        <div className="mb-3">
          <CFormLabel>Chọn Reviewer Mới *</CFormLabel>
          {loadingMembers ? (
            <CSpinner size="sm" />
          ) : (
            <CFormSelect
              value={selectedReviewerId || ''}
              onChange={(e) => setSelectedReviewerId(parseInt(e.target.value) || null)}
            >
              <option value="">-- Chọn reviewer mới --</option>
              {pcMembers.map((member) => {
                const workloadStatus = getWorkloadStatus(member.userId)
                return (
                  <option key={member.id} value={member.userId}>
                    {member.fullName} ({member.email})
                    {workloadStatus && ` - ${workloadStatus}`}
                  </option>
                )
              })}
            </CFormSelect>
          )}
        </div>

        {selectedReviewerId && workloads[selectedReviewerId] && (
          <div className="mb-3">
            <CAlert color="info">
              <strong>Workload hiện tại:</strong>
              <br />
              Tổng assignments: {workloads[selectedReviewerId].totalAssignments} /{' '}
              {workloads[selectedReviewerId].maxAssignments}
              <br />
              Trạng thái: {getWorkloadBadge(workloads[selectedReviewerId].workloadStatus)}
              {workloads[selectedReviewerId].workloadStatus === 'OVERLOADED' && (
                <CAlert color="danger" className="mt-2">
                  Reviewer này đã quá tải và không thể được assign thêm.
                </CAlert>
              )}
              {workloads[selectedReviewerId].workloadStatus === 'HIGH' && (
                <CAlert color="warning" className="mt-2">
                  Reviewer này đang có workload cao.
                </CAlert>
              )}
            </CAlert>
          </div>
        )}

        <div className="mb-3">
          <CFormLabel>Lý do reassign (tùy chọn)</CFormLabel>
          <CFormTextarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            placeholder="Nhập lý do reassign assignment này..."
          />
        </div>
      </CModalBody>
      <CModalFooter>
        <CButton color="secondary" onClick={handleClose} disabled={loading}>
          Hủy
        </CButton>
        <CButton
          color="primary"
          onClick={handleSubmit}
          disabled={loading || !selectedReviewerId || loadingMembers}
        >
          {loading ? <CSpinner size="sm" /> : 'Reassign'}
        </CButton>
      </CModalFooter>
    </CModal>
  )
}

export default ReassignmentModal
