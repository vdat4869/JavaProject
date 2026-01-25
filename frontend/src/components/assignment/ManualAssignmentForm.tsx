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
  CFormCheck,
  CAlert,
  CSpinner,
} from '@coreui/react'
import { assignmentService, AssignmentCreateDTO } from '../../services/assignment.service'
import { pcService, PCMember } from '../../services/pc.service'
import { pcService as pcServiceForWorkload, Workload } from '../../services/pc.service'

interface ManualAssignmentFormProps {
  visible: boolean
  onClose: () => void
  submissionId: number
  conferenceId: number
  onSuccess: () => void
}

/**
 * ManualAssignmentForm - Form để tạo assignment thủ công
 */
const ManualAssignmentForm: React.FC<ManualAssignmentFormProps> = ({
  visible,
  onClose,
  submissionId,
  conferenceId,
  onSuccess,
}) => {
  const [pcMembers, setPCMembers] = useState<PCMember[]>([])
  const [selectedReviewerId, setSelectedReviewerId] = useState<number | null>(null)
  const [isPrimary, setIsPrimary] = useState(false)
  const [loading, setLoading] = useState(false)
  const [loadingMembers, setLoadingMembers] = useState(false)
  const [error, setError] = useState('')
  const [workloads, setWorkloads] = useState<Record<number, Workload>>({})

  useEffect(() => {
    if (visible && conferenceId) {
      loadPCMembers()
    }
  }, [visible, conferenceId])

  const loadPCMembers = async () => {
    try {
      setLoadingMembers(true)
      const members = await pcService.getPCMembers(conferenceId)
      // Only show accepted PC members
      const acceptedMembers = members.filter((m) => m.status === 'ACCEPTED')
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
    if (!selectedReviewerId) {
      setError('Vui lòng chọn reviewer')
      return
    }

    try {
      setLoading(true)
      setError('')
      const data: AssignmentCreateDTO = {
        submissionId,
        reviewerId: selectedReviewerId,
        isPrimary,
      }
      await assignmentService.createAssignment(data)
      onSuccess()
      handleClose()
    } catch (error: any) {
      setError(
        error.response?.data?.message ||
          error.response?.data?.error ||
          'Không thể tạo assignment'
      )
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setSelectedReviewerId(null)
    setIsPrimary(false)
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

  return (
    <CModal visible={visible} onClose={handleClose} size="lg">
      <CModalHeader>
        <CModalTitle>Tạo Assignment Thủ Công</CModalTitle>
      </CModalHeader>
      <CModalBody>
        {error && (
          <CAlert color="danger" className="mb-3" onClose={() => setError('')} dismissible>
            {error}
          </CAlert>
        )}

        <div className="mb-3">
          <CFormLabel>Chọn Reviewer *</CFormLabel>
          {loadingMembers ? (
            <CSpinner size="sm" />
          ) : (
            <CFormSelect
              value={selectedReviewerId || ''}
              onChange={(e) => setSelectedReviewerId(parseInt(e.target.value) || null)}
            >
              <option value="">-- Chọn reviewer --</option>
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
          <CFormCheck
            id="isPrimary"
            label="Đánh dấu là Primary Reviewer"
            checked={isPrimary}
            onChange={(e) => setIsPrimary(e.target.checked)}
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
          {loading ? <CSpinner size="sm" /> : 'Tạo Assignment'}
        </CButton>
      </CModalFooter>
    </CModal>
  )
}

export default ManualAssignmentForm
