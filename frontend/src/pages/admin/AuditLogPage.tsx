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
  CInputGroup,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilSearch, cilReload, cilDownload } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { auditService, AuditLogDTO, AuditLogQueryParams } from '../../services/audit.service'

/**
 * AuditLogPage - Trang xem audit logs (ADMIN only)
 *
 * Features:
 * - Danh sách audit logs với filters
 * - Export audit logs to CSV
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

  const [filters, setFilters] = useState<AuditLogQueryParams>({
    page: 0,
    size: 20,
    sortBy: 'timestamp',
    sortDir: 'DESC',
  })

  const pageSize = 20

  useEffect(() => {
    loadLogs()
  }, [currentPage, filters.userId, filters.action, filters.resource, filters.startDate, filters.endDate])

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

  const handleFilterChange = (name: string, value: string | number | undefined) => {
    setFilters((prev) => ({
      ...prev,
      [name]: value,
    }))
    setCurrentPage(0)
  }

  const handleExport = async () => {
    try {
      setExporting(true)
      const params: AuditLogQueryParams = {
        ...filters,
        page: 0,
        size: 10000, // Export all
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

      <CCard className="mb-3">
        <CCardHeader>
          <h5>{t('admin.filters') || 'Bộ lọc'}</h5>
        </CCardHeader>
        <CCardBody>
          <div className="row g-3">
            <div className="col-md-3">
              <CFormInput
                type="number"
                placeholder={t('admin.userId') || 'User ID'}
                value={filters.userId || ''}
                onChange={(e) =>
                  handleFilterChange('userId', e.target.value ? parseInt(e.target.value) : undefined)
                }
              />
            </div>
            <div className="col-md-3">
              <CFormInput
                type="text"
                placeholder={t('admin.action') || 'Action'}
                value={filters.action || ''}
                onChange={(e) => handleFilterChange('action', e.target.value || undefined)}
              />
            </div>
            <div className="col-md-3">
              <CFormInput
                type="text"
                placeholder={t('admin.resource') || 'Resource'}
                value={filters.resource || ''}
                onChange={(e) => handleFilterChange('resource', e.target.value || undefined)}
              />
            </div>
            <div className="col-md-3">
              <CFormInput
                type="datetime-local"
                placeholder={t('admin.startDate') || 'Start Date'}
                value={filters.startDate || ''}
                onChange={(e) => handleFilterChange('startDate', e.target.value || undefined)}
              />
            </div>
            <div className="col-md-3">
              <CFormInput
                type="datetime-local"
                placeholder={t('admin.endDate') || 'End Date'}
                value={filters.endDate || ''}
                onChange={(e) => handleFilterChange('endDate', e.target.value || undefined)}
              />
            </div>
            <div className="col-md-3">
              <CFormSelect
                value={filters.sortDir || 'DESC'}
                onChange={(e) => handleFilterChange('sortDir', e.target.value as 'ASC' | 'DESC')}
              >
                <option value="DESC">{t('common.descending') || 'Giảm dần'}</option>
                <option value="ASC">{t('common.ascending') || 'Tăng dần'}</option>
              </CFormSelect>
            </div>
            <div className="col-md-3">
              <CButton color="primary" onClick={loadLogs} className="w-100">
                <CIcon icon={cilSearch} /> {t('common.search') || 'Tìm kiếm'}
              </CButton>
            </div>
            <div className="col-md-3">
              <CButton color="secondary" onClick={handleExport} disabled={exporting} className="w-100">
                <CIcon icon={cilDownload} />{' '}
                {exporting ? t('common.exporting') || 'Đang export...' : t('common.export') || 'Export CSV'}
              </CButton>
            </div>
          </div>
        </CCardBody>
      </CCard>

      <CCard>
        <CCardHeader>
          <div className="d-flex justify-content-between align-items-center">
            <h5>{t('admin.auditLogList') || 'Danh sách Audit Logs'}</h5>
            <CButton color="secondary" size="sm" onClick={loadLogs}>
              <CIcon icon={cilReload} /> {t('common.refresh') || 'Làm mới'}
            </CButton>
          </div>
        </CCardHeader>
        <CCardBody>
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
                    <CTableHeaderCell>{t('common.user') || 'User'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('admin.action') || 'Action'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('admin.resource') || 'Resource'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('admin.resourceId') || 'Resource ID'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('admin.details') || 'Details'}</CTableHeaderCell>
                    <CTableHeaderCell>{t('common.timestamp') || 'Timestamp'}</CTableHeaderCell>
                  </CTableRow>
                </CTableHead>
                <CTableBody>
                  {logs.length === 0 ? (
                    <CTableRow>
                      <CTableDataCell colSpan={7} className="text-center">
                        {t('admin.noAuditLogs') || 'Không có audit log nào'}
                      </CTableDataCell>
                    </CTableRow>
                  ) : (
                    logs.map((log) => (
                      <CTableRow key={log.id}>
                        <CTableDataCell>{log.id}</CTableDataCell>
                        <CTableDataCell>
                          {log.username} ({log.userId})
                        </CTableDataCell>
                        <CTableDataCell>
                          <code>{log.action}</code>
                        </CTableDataCell>
                        <CTableDataCell>{log.resource || '-'}</CTableDataCell>
                        <CTableDataCell>{log.resourceId || '-'}</CTableDataCell>
                        <CTableDataCell>
                          <small>{log.details || '-'}</small>
                        </CTableDataCell>
                        <CTableDataCell>
                          <small>{formatDate(log.timestamp)}</small>
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
                {t('common.showing') || 'Hiển thị'} {logs.length} / {totalElements}{' '}
                {t('admin.auditLogs') || 'audit logs'}
              </div>
            </>
          )}
        </CCardBody>
      </CCard>
    </div>
  )
}

export default AuditLogPage
