import React, { useState, useEffect, useCallback } from 'react'
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
  CFormInput,
  CFormLabel,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CAlert,
  CSpinner,
  CBadge,
} from '@coreui/react'
import { pcService, PCMember, InvitePCRequest } from '../../services/pc.service'

/**
 * PCManagement - Trang quản lý PC members
 *
 * Features:
 * - Xem danh sách PC members
 * - Mời PC member mới
 * - Xem trạng thái invitation
 */
const PCManagement: React.FC = () => {
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [members, setMembers] = useState<PCMember[]>([])
  const [loading, setLoading] = useState(true)
  const [showInviteModal, setShowInviteModal] = useState(false)
  const [inviteEmail, setInviteEmail] = useState('')
  const [inviteName, setInviteName] = useState('')
  const [inviting, setInviting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const loadMembers = useCallback(async () => {
    try {
      setLoading(true)
      const data = await pcService.getPCMembers(conferenceId!)
      setMembers(data)
    } catch (error) {
      console.error('Error loading PC members:', error)
    } finally {
      setLoading(false)
    }
  }, [conferenceId])

  useEffect(() => {
    if (conferenceId) {
      loadMembers()
    }
  }, [conferenceId, loadMembers])

  const handleInvite = async () => {
    if (!inviteEmail.trim()) {
      setError('Vui lòng nhập email')
      return
    }

    try {
      setInviting(true)
      setError('')
      await pcService.invitePC({
        conferenceId: conferenceId!,
        email: inviteEmail.trim(),
        fullName: inviteName.trim() || undefined,
      })
      setSuccess('Đã gửi lời mời')
      setInviteEmail('')
      setInviteName('')
      setShowInviteModal(false)
      await loadMembers()
    } catch (error: any) {
      setError(error.response?.data?.message || 'Không thể gửi lời mời')
    } finally {
      setInviting(false)
    }
  }

  const getStatusBadge = (status: PCMember['status']) => {
    const colorMap: Record<string, string> = {
      PENDING: 'warning',
      ACCEPTED: 'success',
      DECLINED: 'danger',
    }
    return <CBadge color={colorMap[status] || 'secondary'}>{status}</CBadge>
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
    <>
      <CCard>
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h4>Quản lý PC Members</h4>
            <CButton color="primary" onClick={() => setShowInviteModal(true)}>
              Mời PC Member
            </CButton>
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

          {members.length === 0 ? (
            <p className="text-muted">Chưa có PC member nào</p>
          ) : (
            <CTable hover>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Email</CTableHeaderCell>
                  <CTableHeaderCell>Họ tên</CTableHeaderCell>
                  <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                  <CTableHeaderCell>Ngày mời</CTableHeaderCell>
                  <CTableHeaderCell>Ngày phản hồi</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {members.map((member) => (
                  <CTableRow key={member.id}>
                    <CTableDataCell>{member.email}</CTableDataCell>
                    <CTableDataCell>{member.fullName}</CTableDataCell>
                    <CTableDataCell>{getStatusBadge(member.status)}</CTableDataCell>
                    <CTableDataCell>
                      {new Date(member.invitedAt).toLocaleDateString('vi-VN')}
                    </CTableDataCell>
                    <CTableDataCell>
                      {member.respondedAt
                        ? new Date(member.respondedAt).toLocaleDateString('vi-VN')
                        : '-'}
                    </CTableDataCell>
                  </CTableRow>
                ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      {/* Invite Modal */}
      <CModal visible={showInviteModal} onClose={() => setShowInviteModal(false)}>
        <CModalHeader>
          <CModalTitle>Mời PC Member</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {error && (
            <CAlert color="danger" className="mb-3">
              {error}
            </CAlert>
          )}
          <div className="mb-3">
            <CFormLabel>Email *</CFormLabel>
            <CFormInput
              type="email"
              value={inviteEmail}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setInviteEmail(e.target.value)}
              placeholder="email@example.com"
              required
            />
          </div>
          <div className="mb-3">
            <CFormLabel>Họ tên (tùy chọn)</CFormLabel>
            <CFormInput
              type="text"
              value={inviteName}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setInviteName(e.target.value)}
              placeholder="Họ và tên"
            />
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowInviteModal(false)}>
            Hủy
          </CButton>
          <CButton color="primary" onClick={handleInvite} disabled={inviting}>
            {inviting ? <CSpinner size="sm" /> : 'Gửi lời mời'}
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default PCManagement
