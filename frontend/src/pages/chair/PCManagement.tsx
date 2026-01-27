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
  CFormSelect,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
  CAlert,
  CSpinner,
  CBadge,
  CNav,
  CNavItem,
  CNavLink,
  CTabs,
  CTabContent,
  CTabPane,
} from '@coreui/react'
import { pcService, PCMember, InvitePCRequest, PCInvitation } from '../../services/pc.service'
import { userService, UserDTO } from '../../services/user.service'

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
  const [invitations, setInvitations] = useState<PCInvitation[]>([])
  const [loading, setLoading] = useState(true)
  const [showInviteModal, setShowInviteModal] = useState(false)
  const [inviteEmail, setInviteEmail] = useState('')
  const [inviting, setInviting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [activeTab, setActiveTab] = useState<'members' | 'invitations'>('members')
  const [allUsers, setAllUsers] = useState<UserDTO[]>([])
  const [loadingUsers, setLoadingUsers] = useState(false)

  const loadMembers = useCallback(async () => {
    try {
      setLoading(true)
      const [membersData, invitationsData] = await Promise.all([
        pcService.getPCMembers(conferenceId!),
        pcService.getInvitations(conferenceId!),
      ])
      setMembers(membersData)
      setInvitations(invitationsData)
    } catch (error) {
      console.error('Error loading PC data:', error)
    } finally {
      setLoading(false)
    }
  }, [conferenceId])

  useEffect(() => {
    if (conferenceId) {
      loadMembers()
    }
  }, [conferenceId, loadMembers])

  useEffect(() => {
    if (showInviteModal) {
      loadUsers()
    }
  }, [showInviteModal])

  const loadUsers = async () => {
    try {
      setLoadingUsers(true)
      const data = await userService.getAllUsers()
      setAllUsers(data.content || [])
    } catch (error) {
      console.error('Error loading users:', error)
    } finally {
      setLoadingUsers(false)
    }
  }

  const handleUserSelect = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setInviteEmail(e.target.value)
  }

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
      })
      setSuccess('Đã gửi lời mời')
      setInviteEmail('')
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
          <CNav variant="tabs">
            <CNavItem>
              <CNavLink
                active={activeTab === 'members'}
                style={{ cursor: 'pointer' }}
                onClick={() => setActiveTab('members')}
              >
                PC Members ({members.length})
              </CNavLink>
            </CNavItem>
            <CNavItem>
              <CNavLink
                active={activeTab === 'invitations'}
                style={{ cursor: 'pointer' }}
                onClick={() => setActiveTab('invitations')}
              >
                Invitations ({invitations.length})
                {invitations.filter((i) => i.status === 'PENDING').length > 0 && (
                  <CBadge color="warning" className="ms-2">
                    {invitations.filter((i) => i.status === 'PENDING').length} pending
                  </CBadge>
                )}
              </CNavLink>
            </CNavItem>
          </CNav>
          <CTabContent>
            <CTabPane visible={activeTab === 'members'}>
              <div className="mt-3">
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
                            {new Date(member.createdAt).toLocaleDateString('vi-VN')}
                          </CTableDataCell>
                          <CTableDataCell>
                            {member.status !== 'PENDING'
                              ? new Date(member.updatedAt).toLocaleDateString('vi-VN')
                              : '-'}
                          </CTableDataCell>
                        </CTableRow>
                      ))}
                    </CTableBody>
                  </CTable>
                )}
              </div>
            </CTabPane>
            <CTabPane visible={activeTab === 'invitations'}>
              <div className="mt-3">
                {invitations.length === 0 ? (
                  <p className="text-muted">Chưa có invitation nào</p>
                ) : (
                  <CTable hover>
                    <CTableHead>
                      <CTableRow>
                        <CTableHeaderCell>Email</CTableHeaderCell>
                        <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                        <CTableHeaderCell>Ngày mời</CTableHeaderCell>
                        <CTableHeaderCell>Hết hạn</CTableHeaderCell>
                      </CTableRow>
                    </CTableHead>
                    <CTableBody>
                      {invitations.map((invitation) => (
                        <CTableRow key={invitation.id}>
                          <CTableDataCell>{invitation.invitedUserEmail}</CTableDataCell>
                          <CTableDataCell>
                            <CBadge
                              color={
                                invitation.status === 'PENDING'
                                  ? 'warning'
                                  : invitation.status === 'ACCEPTED'
                                    ? 'success'
                                    : 'danger'
                              }
                            >
                              {invitation.status}
                            </CBadge>
                          </CTableDataCell>
                          <CTableDataCell>
                            {new Date(invitation.createdAt).toLocaleDateString('vi-VN')}
                          </CTableDataCell>
                          <CTableDataCell>
                            {new Date(invitation.expiresAt).toLocaleDateString('vi-VN')}
                            {new Date(invitation.expiresAt) < new Date() &&
                              invitation.status === 'PENDING' && (
                                <CBadge color="danger" className="ms-2">
                                  Hết hạn
                                </CBadge>
                              )}
                          </CTableDataCell>
                        </CTableRow>
                      ))}
                    </CTableBody>
                  </CTable>
                )}
              </div>
            </CTabPane>
          </CTabContent>
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
            <CFormLabel>Chọn từ hệ thống</CFormLabel>
            <CFormSelect onChange={handleUserSelect} disabled={loadingUsers}>
              <option value="">-- Chọn user --</option>
              {allUsers.map((u) => (
                <option key={u.id} value={u.email}>
                  {u.firstName} {u.lastName} ({u.email})
                </option>
              ))}
            </CFormSelect>
            {loadingUsers && <CSpinner size="sm" className="mt-2" />}
          </div>
          <div className="mb-3">
            <CFormLabel>Hoặc nhập Email mới *</CFormLabel>
            <CFormInput
              type="email"
              value={inviteEmail}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setInviteEmail(e.target.value)}
              placeholder="email@example.com"
              required
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

