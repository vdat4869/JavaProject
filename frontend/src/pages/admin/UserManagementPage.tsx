import React, { useState, useEffect } from 'react'
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
  CInputGroup,
  CFormInput,
  CSpinner,
  CAlert,
  CPagination,
  CPaginationItem,
  CBadge,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilSearch, cilCheckCircle, cilXCircle, cilReload, cilShieldAlt } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { userService, UserDTO, UserStats } from '../../services/user.service'
import { CFormCheck } from '@coreui/react'

/**
 * UserManagementPage - Trang quản lý user (ADMIN only)
 *
 * Features:
 * - Danh sách tất cả users
 * - Tìm kiếm users
 * - Activate/Deactivate users
 * - Xem thống kê users
 */
const UserManagementPage: React.FC = () => {
  const { t } = useTranslation()
  const [users, setUsers] = useState<UserDTO[]>([])
  const [stats, setStats] = useState<UserStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [error, setError] = useState('')
  const [actionLoading, setActionLoading] = useState<number | null>(null)
  const [showConfirmModal, setShowConfirmModal] = useState(false)
  const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null)
  const [actionType, setActionType] = useState<'activate' | 'deactivate' | null>(null)
  const [showRoleModal, setShowRoleModal] = useState(false)
  const [selectedRoles, setSelectedRoles] = useState<string[]>([])
  const [roleSaving, setRoleSaving] = useState(false)

  const availableRoles = ['ADMIN', 'CHAIR', 'PC', 'AUTHOR']

  const pageSize = 20

  useEffect(() => {
    loadUsers()
    loadStats()
  }, [currentPage])

  const loadUsers = async () => {
    try {
      setLoading(true)
      setError('')
      let response
      if (searchKeyword.trim()) {
        response = await userService.searchUsers(searchKeyword, currentPage, pageSize)
      } else {
        response = await userService.getAllUsers(currentPage, pageSize)
      }
      setUsers(response.content || [])
      setTotalPages(response.totalPages || 0)
      setTotalElements(response.totalElements || 0)
    } catch (err: any) {
      setError(err.response?.data?.message || t('admin.loadUsersError'))
    } finally {
      setLoading(false)
    }
  }

  const loadStats = async () => {
    try {
      const statsData = await userService.getUserStats()
      setStats(statsData)
    } catch (err) {
      // Ignore stats error
    }
  }

  const handleSearch = () => {
    setCurrentPage(0)
    loadUsers()
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch()
    }
  }

  const handleActivate = (user: UserDTO) => {
    setSelectedUser(user)
    setActionType('activate')
    setShowConfirmModal(true)
  }

  const handleDeactivate = (user: UserDTO) => {
    setSelectedUser(user)
    setActionType('deactivate')
    setShowConfirmModal(true)
  }

  const confirmAction = async () => {
    if (!selectedUser || !actionType) return

    try {
      setActionLoading(selectedUser.id)
      if (actionType === 'activate') {
        await userService.activateUser(selectedUser.id)
      } else {
        await userService.deactivateUser(selectedUser.id)
      }
      setShowConfirmModal(false)
      setSelectedUser(null)
      setActionType(null)
      await loadUsers()
      await loadStats()
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error'))
    } finally {
      setActionLoading(null)
    }
  }

  const handleManageRoles = (user: UserDTO) => {
    setSelectedUser(user)
    setSelectedRoles(user.roles || [])
    setShowRoleModal(true)
  }

  const handleRoleToggle = (role: string) => {
    if (selectedRoles.includes(role)) {
      setSelectedRoles(selectedRoles.filter((r) => r !== role))
    } else {
      setSelectedRoles([...selectedRoles, role])
    }
  }

  const saveRoles = async () => {
    if (!selectedUser) return

    try {
      setRoleSaving(true)
      await userService.updateUserRoles(selectedUser.id, selectedRoles)
      setShowRoleModal(false)
      await loadUsers()
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error'))
    } finally {
      setRoleSaving(false)
    }
  }

  return (
    <div>
      <h2 className="mb-4">{t('admin.userManagement')}</h2>

      {stats && (
        <div className="row mb-4">
          <div className="col-md-6">
            <CCard>
              <CCardBody>
                <h5>{t('admin.activeUsers')}</h5>
                <h3>{stats.activeUsers}</h3>
              </CCardBody>
            </CCard>
          </div>
          <div className="col-md-6">
            <CCard>
              <CCardBody>
                <h5>{t('admin.verifiedUsers')}</h5>
                <h3>{stats.verifiedUsers}</h3>
              </CCardBody>
            </CCard>
          </div>
        </div>
      )}

      <CCard>
        <CCardHeader className="bg-white">
          <div className="d-flex justify-content-between align-items-center">
            <h5>{t('admin.userList')}</h5>
            <CButton color="secondary" size="sm" onClick={loadUsers}>
              <CIcon icon={cilReload} /> {t('common.refresh')}
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          <div className="mb-3">
            <CInputGroup>
              <CFormInput
                placeholder={t('admin.searchUsers')}
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyPress={handleKeyPress}
              />
              <CButton color="primary" onClick={handleSearch}>
                <CIcon icon={cilSearch} /> {t('common.search')}
              </CButton>
            </CInputGroup>
          </div>

          {error && (
            <CAlert color="danger" className="mb-3">
              {error}
            </CAlert>
          )}

          {loading ? (
            <div className="text-center py-5">
              <CSpinner color="primary" />
            </div>
          ) : (
            <>
              <CTable hover responsive>
                <CTableHead color="light">
                  <CTableRow>
                    <CTableHeaderCell>{t('common.id')}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.email')}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.name')}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.organization')}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.status')}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.roles')}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.actions')}</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {users.length === 0 ? (
                    <CTableRow>
                      <CTableDataCell colSpan={7} className="text-center">
                        {t('admin.noUsers')}
                      </CTableDataCell>
                    </CTableRow>
                  ) : (
                    users.map((user) => (
                      <CTableRow key={user.id}>
                        <CTableDataCell>{user.id}</CTableDataCell>
                        <CTableDataCell>{user.email}</CTableDataCell>
                        <CTableDataCell>
                          {user.firstName} {user.lastName}
                        </CTableDataCell>
                        <CTableDataCell>{user.organizationName || '-'}</CTableDataCell>
                        <CTableDataCell>
                          {user.active ? (
                            <CBadge color="success">
                              <CIcon icon={cilCheckCircle} /> {t('common.active')}
                            </CBadge>
                          ) : (
                            <CBadge color="danger">
                              <CIcon icon={cilXCircle} /> {t('common.inactive')}
                            </CBadge>
                          )}
                          {user.emailVerified && (
                            <CBadge color="info" className="ms-2">
                              {t('common.verified')}
                            </CBadge>
                          )}
                        </CTableDataCell>
                        <CTableDataCell>
                          {user.roles
                            ? user.roles.map((r: string) => t(`common.roleNames.${r}`) || r).join(', ')
                            : t('common.noRoles')}
                        </CTableDataCell>
                        <CTableDataCell>
                          {user.active ? (
                            <CButton
                              color="warning"
                              size="sm"
                              className="me-2"
                              onClick={() => handleDeactivate(user)}
                              disabled={actionLoading === user.id}
                            >
                              {t('admin.deactivate')}
                            </CButton>
                          ) : (
                            <CButton
                              color="success"
                              size="sm"
                              className="me-2"
                              onClick={() => handleActivate(user)}
                              disabled={actionLoading === user.id}
                            >
                              {t('admin.activate')}
                            </CButton>
                          )}
                          <CButton color="info" size="sm" onClick={() => handleManageRoles(user)}>
                            <CIcon icon={cilShieldAlt} /> {t('admin.manageRoles')}
                          </CButton>
                        </CTableDataCell>
                      </CTableRow>
                    ))
                  )}
                </CTableBody>
              </CTable>

              {totalPages > 1 && (
                <CPagination className="mt-3">
                  <CPaginationItem
                    disabled={currentPage === 0}
                    onClick={() => setCurrentPage(currentPage - 1)}
                  >
                    {t('common.previous')}
                  </CPaginationItem>
                  {Array.from({ length: totalPages }, (_, i) => i).map((page) => (
                    <CPaginationItem
                      key={page}
                      active={page === currentPage}
                      onClick={() => setCurrentPage(page)}
                    >
                      {page + 1}
                    </CPaginationItem>
                  ))}
                  <CPaginationItem
                    disabled={currentPage >= totalPages - 1}
                    onClick={() => setCurrentPage(currentPage + 1)}
                  >
                    {t('common.next')}
                  </CPaginationItem>
                </CPagination>
              )}

              <div className="mt-3 text-muted">
                {t('common.showing')} {users.length} / {totalElements}{' '}
                {t('common.users')}
              </div>
            </>
          )}
        </CCardBody>
      </CCard>

      <CModal visible={showConfirmModal} onClose={() => setShowConfirmModal(false)}>
        <CModalHeader>
          <CModalTitle>
            {actionType === 'activate'
              ? t('admin.confirmActivate')
              : t('admin.confirmDeactivate')}
          </CModalTitle>
        </CModalHeader>
        <CModalBody>
          {actionType === 'activate'
            ? t('admin.confirmActivateMessage')
            : t('admin.confirmDeactivateMessage')}
          <br />
          <strong>{selectedUser?.email}</strong>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowConfirmModal(false)}>
            {t('common.cancel')}
          </CButton>
          <CButton
            color={actionType === 'activate' ? 'success' : 'warning'}
            onClick={confirmAction}
            disabled={actionLoading !== null}
          >
            {actionLoading !== null ? (
              <CSpinner size="sm" />
            ) : actionType === 'activate' ? (
              t('admin.activate')
            ) : (
              t('admin.deactivate')
            )}
          </CButton>
        </CModalFooter>
      </CModal>

      <CModal visible={showRoleModal} onClose={() => setShowRoleModal(false)}>
        <CModalHeader>
          <CModalTitle>{t('admin.manageRoles')}</CModalTitle>
        </CModalHeader>
        <CModalBody>
          <p>
            {t('admin.selectRoles')} <strong>{selectedUser?.email}</strong>
          </p>
          <div className="mt-3">
            {availableRoles.map((role) => (
              <CFormCheck
                key={role}
                id={`role-${role}`}
                label={t(`common.roleNames.${role}`) || role}
                checked={selectedRoles.includes(role)}
                onChange={() => handleRoleToggle(role)}
                className="mb-2"
              />
            ))}
          </div>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowRoleModal(false)}>
            {t('common.cancel')}
          </CButton>
          <CButton color="primary" onClick={saveRoles} disabled={roleSaving}>
            {roleSaving ? <CSpinner size="sm" /> : t('common.save')}
          </CButton>
        </CModalFooter>
      </CModal>
    </div>
  )
}

export default UserManagementPage
