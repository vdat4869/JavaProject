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
  CFormInput,
  CFormSelect,
  CSpinner,
  CAlert,
  CPagination,
  CPaginationItem,
  CBadge,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilSearch, cilReload, cilCloudDownload, cilInfo } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { auditService, AuditLogDTO, AuditLogQueryParams } from '../../services/audit.service'

/**
 * AuditLogPage - Trang xem audit logs (ADMIN only)
 */
const AuditLogPage: React.FC = () => {
  const { t } = useTranslation()
  const [logs, setLogs] = useState<AuditLogDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [exporting, setExporting] = useState(false)
  const [error, setError] = useState('')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  // Detail modal states
  const [selectedLog, setSelectedLog] = useState<AuditLogDTO | null>(null)
  const [showModal, setShowModal] = useState(false)

  const [filters, setFilters] = useState<AuditLogQueryParams>({
    page: 0,
    size: 20,
    sortBy: 'timestamp',
    sortDir: 'DESC',
  })

  const pageSize = 20

  useEffect(() => {
    loadLogs()
  }, [currentPage, filters.userId, filters.action, filters.resource, filters.resourceId, filters.startDate, filters.endDate])

  const loadLogs = async () => {
    try {
      setLoading(true)
      setError('')
      const params: AuditLogQueryParams = {
        ...filters,
        page: currentPage,
        size: pageSize,
      }
      const response = await auditService.getAuditLogs(params)
      setLogs(response.content || [])
      setTotalPages(response.totalPages || 0)
      setTotalElements(response.totalElements || 0)
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi tải audit logs')
    } finally {
      setLoading(false)
    }
  }

  const handleFilterChange = (name: string, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [name]: value === '' ? undefined : value,
    }))
    setCurrentPage(0)
  }

  const handleExport = async () => {
    try {
      setExporting(true)
      const params: AuditLogQueryParams = {
        ...filters,
        page: 0,
        size: 10000,
      }
      const blob = await auditService.exportAuditLogs(params, 'CSV')
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `audit-logs-${new Date().toISOString().split('T')[0]}.csv`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } catch (err: any) {
      setError(err.response?.data?.message || t('common.error') || 'Lỗi khi export audit logs')
    } finally {
      setExporting(false)
    }
  }

  const handleShowDetail = (log: AuditLogDTO) => {
    setSelectedLog(log)
    setShowModal(true)
  }

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleString('vi-VN')
    } catch {
      return dateString
    }
  }

  return (
    <div>
      <h2 className="mb-4">{t('admin.auditLogs') || 'Audit Logs'}</h2>

      <CCard className="mb-3 shadow-sm">
        <CCardHeader>
          <h5>{t('admin.filters') || 'Bộ lọc nâng cao'}</h5>
        </CCardHeader>
        <CCardBody>
          <div className="row g-3">
            <div className="col-md-2">
              <CFormInput
                type="number"
                placeholder="User ID"
                value={filters.userId || ''}
                onChange={(e) => handleFilterChange('userId', e.target.value ? parseInt(e.target.value) : undefined)}
              />
            </div>
            <div className="col-md-2">
              <CFormInput
                type="text"
                placeholder="Action (e.target.value)"
                value={filters.action || ''}
                onChange={(e) => handleFilterChange('action', e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <CFormInput
                type="text"
                placeholder="Resource"
                value={filters.resource || ''}
                onChange={(e) => handleFilterChange('resource', e.target.value)}
              />
            </div>
            <div className="col-md-2">
              <CFormInput
                type="number"
                placeholder="Resource ID"
                value={filters.resourceId || ''}
                onChange={(e) => handleFilterChange('resourceId', e.target.value ? parseInt(e.target.value) : undefined)}
              />
            </div>
            <div className="col-md-2">
              <CFormInput
                type="datetime-local"
                value={filters.startDate || ''}
                onChange={(e) => handleFilterChange('startDate', e.target.value)}
                label="Từ ngày"
              />
            </div>
            <div className="col-md-2">
              <CFormInput
                type="datetime-local"
                value={filters.endDate || ''}
                onChange={(e) => handleFilterChange('endDate', e.target.value)}
                label="Đến ngày"
              />
            </div>
            <div className="col-md-12 d-flex justify-content-end gap-2">
              <CButton color="secondary" variant="outline" onClick={() => setFilters({ page: 0, size: 20, sortBy: 'timestamp', sortDir: 'DESC' })}>
                Xóa lọc
              </CButton>
              <CButton color="primary" onClick={loadLogs}>
                <CIcon icon={cilSearch} /> {t('common.search') || 'Tìm kiếm'}
              </CButton>
              <CButton color="success" onClick={handleExport} disabled={exporting}>
                <CIcon icon={cilCloudDownload} /> Export
              </CButton>
            </div>
          </div>
        </CCardBody>
      </CCard>

      <CCard className="shadow-sm">
        <CCardHeader className="d-flex justify-content-between align-items-center">
          <h5>{t('admin.auditLogList') || 'Danh sách Hoạt động'}</h5>
          <CButton color="light" size="sm" onClick={loadLogs}>
            <CIcon icon={cilReload} /> Làm mới
          </CButton>
        </CCardHeader>
        <CCardBody>
          {error && <CAlert color="danger" dismissible onClose={() => setError('')}>{error}</CAlert>}

          {loading ? (
            <div className="text-center py-5"><CSpinner color="primary" /></div>
          ) : (
            <>
              <CTable hover responsive align="middle" className="mb-0">
                <CTableHead color="light">
                  <CTableRow>
                    <CTableHeaderCell>Thời gian</CTableHeaderCell>
                    <CTableHeaderCell>Người dùng</CTableHeaderCell>
                    <CTableHeaderCell>Hành động</CTableHeaderCell>
                    <CTableHeaderCell>IP Address</CTableHeaderCell>
                    <CTableHeaderCell>Đối tượng</CTableHeaderCell>
                    <CTableHeaderCell>Thao tác</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {logs.length === 0 ? (
                    <CTableRow><CTableDataCell colSpan={6} className="text-center py-4 text-muted">Không tìm thấy bản ghi nào</CTableDataCell></CTableRow>
                  ) : (
                    logs.map((log) => (
                      <CTableRow key={log.id}>
                        <CTableDataCell><small>{formatDate(log.timestamp)}</small></CTableDataCell>
                        <CTableDataCell>
                          <div><strong>{log.username}</strong></div>
                          <small className="text-muted">ID: {log.userId}</small>
                        </CTableDataCell>
                        <CTableDataCell><CBadge color="info">{log.action}</CBadge></CTableDataCell>
                        <CTableDataCell><small>{log.ipAddress || '-'}</small></CTableDataCell>
                        <CTableDataCell>
                          <div>{log.resource || '-'}</div>
                          {log.resourceId && <CBadge color="secondary">ID: {log.resourceId}</CBadge>}
                        </CTableDataCell>
                        <CTableDataCell>
                          <CButton color="info" variant="ghost" size="sm" onClick={() => handleShowDetail(log)}>
                            <CIcon icon={cilInfo} /> Chi tiết
                          </CButton>
                        </CTableDataCell>
                      </CTableRow>
                    ))
                  )}
                </CTableBody>
              </CTable>

              <div className="d-flex justify-content-between align-items-center mt-3">
                <small className="text-muted">Hiển thị {logs.length} / {totalElements} bản ghi</small>
                {totalPages > 1 && (
                  <CPagination>
                    <CPaginationItem disabled={currentPage === 0} onClick={() => setCurrentPage(currentPage - 1)}>Trước</CPaginationItem>
                    {Array.from({ length: Math.min(5, totalPages) }, (_, i) => i).map((p) => (
                      <CPaginationItem key={p} active={p === currentPage} onClick={() => setCurrentPage(p)}>{p + 1}</CPaginationItem>
                    ))}
                    <CPaginationItem disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage(currentPage + 1)}>Sau</CPaginationItem>
                  </CPagination>
                )}
              </div>
            </>
          )}
        </CCardBody>
      </CCard>

      {/* Detail Modal */}
      <CModal visible={showModal} onClose={() => setShowModal(false)} size="lg">
        <CModalHeader><CModalTitle>Chi tiết Hoạt động #{selectedLog?.id}</CModalTitle></CModalHeader>
        <CModalBody>
          {selectedLog && (
            <div className="audit-detail">
              <div className="row mb-2">
                <div className="col-sm-4 text-muted">Thời gian:</div>
                <div className="col-sm-8">{formatDate(selectedLog.timestamp)}</div>
              </div>
              <div className="row mb-2">
                <div className="col-sm-4 text-muted">Người dùng:</div>
                <div className="col-sm-8">{selectedLog.username} (ID: {selectedLog.userId})</div>
              </div>
              <div className="row mb-2">
                <div className="col-sm-4 text-muted">Hành động:</div>
                <div className="col-sm-8"><CBadge color="info">{selectedLog.action}</CBadge></div>
              </div>
              <div className="row mb-2">
                <div className="col-sm-4 text-muted">Đối tượng:</div>
                <div className="col-sm-8">{selectedLog.resource} {selectedLog.resourceId && `(ID: ${selectedLog.resourceId})`}</div>
              </div>
              <div className="row mb-2">
                <div className="col-sm-4 text-muted">IP Address:</div>
                <div className="col-sm-8"><code>{selectedLog.ipAddress || 'N/A'}</code></div>
              </div>
              <div className="row mb-2">
                <div className="col-sm-4 text-muted">Thiết bị (User Agent):</div>
                <div className="col-sm-8 small">{selectedLog.userAgent || 'N/A'}</div>
              </div>
              <div className="mt-3">
                <div className="text-muted mb-1">Dữ liệu chi tiết:</div>
                <pre className="p-3 bg-light border rounded" style={{ maxHeight: '300px', overflow: 'auto' }}>
                  {selectedLog.details || 'Không có dữ liệu chi tiết'}
                </pre>
              </div>
            </div>
          )}
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowModal(false)}>Đóng</CButton>
        </CModalFooter>
      </CModal>
    </div>
  )
}

export default AuditLogPage
