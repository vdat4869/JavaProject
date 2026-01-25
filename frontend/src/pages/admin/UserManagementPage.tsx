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
import { cilSearch, cilCheckCircle, cilXCircle, cilReload } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { userService, UserDTO, UserStats } from '../../services/user.service'

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
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi tải danh sách users')
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
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi thực hiện thao tác')
    } finally {
      setActionLoading(null)
    }
  }

  return (
    <div>
      <h2 className="mb-4">{t('admin.userManagement') || 'Quản lý người dùng'}</h2>

      {stats && (
        <div className="row mb-4">
          <div className="col-md-6">
            <CCard>
              <CCardBody>
                <h5>{t('admin.activeUsers') || 'Người dùng đang hoạt động'}</h5>
                <h3>{stats.activeUsers}</h3>
              </CCardBody>
            </CCard>
          </div>
          <div className="col-md-6">
            <CCard>
              <CCardBody>
                <h5>{t('admin.verifiedUsers') || 'Người dùng đã xác thực'}</h5>
                <h3>{stats.verifiedUsers}</h3>
              </CCardBody>
            </CCard>
          </div>
        </div>
      )}

      <CCard>
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h5>{t('admin.userList') || 'Danh sách người dùng'}</h5>
            <CButton color="secondary" size="sm" onClick={loadUsers}>
              <CIcon icon={cilReload} /> {t('common.refresh') || 'Làm mới'}
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
          <div className="mb-3">
            <CInputGroup>
              <CFormInput
                placeholder={t('admin.searchUsers') || 'Tìm kiếm theo tên hoặc email...'}
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyPress={handleKeyPress}
              />
              <CButton color="primary" onClick={handleSearch}>
                <CIcon icon={cilSearch} /> {t('common.search') || 'Tìm kiếm'}
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
                <CTableHead>
                  <CTableRow>
                    <CTableHeaderCell>{t('common.id') || 'ID'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.email') || 'Email'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.name') || 'Tên'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.affiliation') || 'Tổ chức'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.status') || 'Trạng thái'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.roles') || 'Vai trò'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.actions') || 'Thao tác'}</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {users.length === 0 ? (
                    <CTableRow>
                      <CTableDataCell colSpan={7} className="text-center">
                        {t('admin.noUsers') || 'Không có người dùng nào'}
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
                        <CTableDataCell>{user.affiliation || '-'}</CTableDataCell>
                        <CTableDataCell>
                          {user.active ? (
                            <CBadge color="success">
                              <CIcon icon={cilCheckCircle} /> {t('common.active') || 'Hoạt động'}
                            </CBadge>
                          ) : (
                            <CBadge color="danger">
                              <CIcon icon={cilXCircle} /> {t('common.inactive') || 'Không hoạt động'}
                            </CBadge>
                          )}
                          {user.emailVerified && (
                            <CBadge color="info" className="ms-2">
                              {t('common.verified') || 'Đã xác thực'}
                            </CBadge>
                          )}
                        </CTableDataCell>
                        <CTableDataCell>
                          {user.roles?.join(', ') || t('common.noRoles') || 'Không có vai trò'}
                        </CTableDataCell>
                        <CTableDataCell>
                          {user.active ? (
                            <CButton
                              color="warning"
                              size="sm"
                              onClick={() => handleDeactivate(user)}
                              disabled={actionLoading === user.id}
                            >
                              {t('admin.deactivate') || 'Vô hiệu hóa'}
                            </CButton>
                          ) : (
                            <CButton
                              color="success"
                              size="sm"
                              onClick={() => handleActivate(user)}
                              disabled={actionLoading === user.id}
                            >
                              {t('admin.activate') || 'Kích hoạt'}
                            </CButton>
                          )}
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
                    {t('common.previous') || 'Trước'}
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
                    {t('common.next') || 'Sau'}
                  </CPaginationItem>
                </CPagination>
              )}

              <div className="mt-3 text-muted">
                {t('common.showing') || 'Hiển thị'} {users.length} / {totalElements}{' '}
                {t('common.users') || 'người dùng'}
              </div>
            </>
          )}
        </CCardBody>
      </CCard>

      <CModal visible={showConfirmModal} onClose={() => setShowConfirmModal(false)}>
        <CModalHeader>
          <CModalTitle>
            {actionType === 'activate'
              ? t('admin.confirmActivate') || 'Xác nhận kích hoạt'
              : t('admin.confirmDeactivate') || 'Xác nhận vô hiệu hóa'}
          </CModalTitle>
        </CModalHeader>
        <CModalBody>
          {actionType === 'activate'
            ? t('admin.confirmActivateMessage') || 'Bạn có chắc chắn muốn kích hoạt người dùng này?'
            : t('admin.confirmDeactivateMessage') || 'Bạn có chắc chắn muốn vô hiệu hóa người dùng này?'}
          <br />
          <strong>{selectedUser?.email}</strong>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowConfirmModal(false)}>
            {t('common.cancel') || 'Hủy'}
          </CButton>
          <CButton
            color={actionType === 'activate' ? 'success' : 'warning'}
            onClick={confirmAction}
            disabled={actionLoading !== null}
          >
            {actionLoading !== null ? (
              <CSpinner size="sm" />
            ) : actionType === 'activate' ? (
              t('admin.activate') || 'Kích hoạt'
            ) : (
              t('admin.deactivate') || 'Vô hiệu hóa'
            )}
          </CButton>
        </CModalFooter>
      </CModal>
    </div>
  )
}

export default UserManagementPage
