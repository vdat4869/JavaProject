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
  CBadge,
  CSpinner,
  CAlert,
  CFormInput,
  CFormSelect,
  CButton,
} from '@coreui/react'
import CIcon from '@coreui/icons-react'
import { cilSearch, cilFilter } from '@coreui/icons'
import { useTranslation } from 'react-i18next'
import { pcService, COIHistory as COIHistoryData, COIType } from '../../services/pc.service'

/**
 * COIHistory - Trang hiển thị lịch sử COI của conference
 *
 * Features:
 * - Hiển thị tất cả COI declarations
 * - Filter theo reviewer, submission, type, action
 * - Export COI data
 */
const COIHistory: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [history, setHistory] = useState<COIHistoryData[]>([])
  const [filteredHistory, setFilteredHistory] = useState<COIHistoryData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterType, setFilterType] = useState<COIType | 'ALL'>('ALL')
  const [filterAction, setFilterAction] = useState<'ALL' | 'DECLARED' | 'REMOVED' | 'AUTO_DETECTED'>(
    'ALL'
  )

  useEffect(() => {
    if (conferenceId) {
      loadHistory()
    }
  }, [conferenceId])

  useEffect(() => {
    applyFilters()
  }, [history, searchTerm, filterType, filterAction])

  const loadHistory = async () => {
    try {
      setLoading(true)
      const data = await pcService.getCOIHistory(conferenceId!)
      setHistory(data)
    } catch (error: any) {
      console.error('Error loading COI history:', error)
      setError(error.response?.data?.message || 'Không thể tải lịch sử COI')
    } finally {
      setLoading(false)
    }
  }

  const applyFilters = () => {
    let filtered = [...history]

    // Filter by search term (reviewer name, email, submission title)
    if (searchTerm) {
      const term = searchTerm.toLowerCase()
      filtered = filtered.filter(
        (item) =>
          item.reviewerName?.toLowerCase().includes(term) ||
          item.reviewerEmail?.toLowerCase().includes(term) ||
          item.submissionTitle?.toLowerCase().includes(term) ||
          item.reason?.toLowerCase().includes(term)
      )
    }

    // Filter by type
    if (filterType !== 'ALL') {
      filtered = filtered.filter((item) => item.coiType === filterType)
    }

    // Filter by action
    if (filterAction !== 'ALL') {
      filtered = filtered.filter((item) => item.action === filterAction)
    }

    setFilteredHistory(filtered)
  }

  const getCOITypeLabel = (type: COIType) => {
    const labels: Record<COIType, string> = {
      CO_AUTHOR: 'Đồng tác giả',
      COLLABORATOR: 'Cộng tác viên',
      ADVISOR: 'Cố vấn',
      INSTITUTIONAL: 'Cùng tổ chức',
      OTHER: 'Khác',
    }
    return labels[type] || type
  }

  const getCOITypeBadge = (type: COIType) => {
    const colorMap: Record<COIType, string> = {
      CO_AUTHOR: 'danger',
      COLLABORATOR: 'warning',
      ADVISOR: 'info',
      INSTITUTIONAL: 'secondary',
      OTHER: 'dark',
    }
    return <CBadge color={colorMap[type] || 'secondary'}>{getCOITypeLabel(type)}</CBadge>
  }

  const getActionBadge = (action: COIHistoryData['action']) => {
    const colorMap: Record<COIHistoryData['action'], string> = {
      DECLARED: 'success',
      REMOVED: 'danger',
      AUTO_DETECTED: 'info',
    }
    const labelMap: Record<COIHistoryData['action'], string> = {
      DECLARED: 'Đã khai báo',
      REMOVED: 'Đã xóa',
      AUTO_DETECTED: 'Tự động phát hiện',
    }
    return <CBadge color={colorMap[action]}>{labelMap[action]}</CBadge>
  }

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <CSpinner color="primary" />
      </div>
    )
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

  if (error) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="danger">{error}</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <CCard>
      <CCardHeader>
        <h4>Lịch sử Conflict of Interest</h4>
      </CCardHeader>
      <CCardBody>
        {/* Filters */}
        <div className="row mb-3">
          <div className="col-md-4">
            <CFormInput
              type="text"
              placeholder="Tìm kiếm reviewer, submission..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="col-md-3">
            <CFormSelect value={filterType} onChange={(e) => setFilterType(e.target.value as any)}>
              <option value="ALL">Tất cả loại COI</option>
              <option value="CO_AUTHOR">Đồng tác giả</option>
              <option value="COLLABORATOR">Cộng tác viên</option>
              <option value="ADVISOR">Cố vấn</option>
              <option value="INSTITUTIONAL">Cùng tổ chức</option>
              <option value="OTHER">Khác</option>
            </CFormSelect>
          </div>
          <div className="col-md-3">
            <CFormSelect
              value={filterAction}
              onChange={(e) => setFilterAction(e.target.value as any)}
            >
              <option value="ALL">Tất cả hành động</option>
              <option value="DECLARED">Đã khai báo</option>
              <option value="REMOVED">Đã xóa</option>
              <option value="AUTO_DETECTED">Tự động phát hiện</option>
            </CFormSelect>
          </div>
          <div className="col-md-2">
            <CButton color="secondary" onClick={() => loadHistory()}>
              <CIcon icon={cilFilter} className="me-1" />
              Làm mới
            </CButton>
          </div>
        </div>

        {filteredHistory.length === 0 ? (
          <div className="text-center py-5">
            <p className="text-muted">
              {history.length === 0
                ? 'Chưa có lịch sử COI nào'
                : 'Không tìm thấy kết quả phù hợp'}
            </p>
          </div>
        ) : (
          <CTable hover responsive>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>Reviewer</CTableHeaderCell>
                <CTableHeaderCell>Submission</CTableHeaderCell>
                <CTableHeaderCell>Loại COI</CTableHeaderCell>
                <CTableHeaderCell>Lý do</CTableHeaderCell>
                <CTableHeaderCell>Hành động</CTableHeaderCell>
                <CTableHeaderCell>Trạng thái</CTableHeaderCell>
                <CTableHeaderCell>Ngày</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {filteredHistory.map((item) => (
                <CTableRow key={item.id}>
                  <CTableDataCell>
                    <div>
                      <strong>{item.reviewerName}</strong>
                      <br />
                      <small className="text-muted">{item.reviewerEmail}</small>
                    </div>
                  </CTableDataCell>
                  <CTableDataCell>
                    <div>
                      <strong>#{item.submissionId}</strong>
                      <br />
                      <small className="text-muted">{item.submissionTitle || '-'}</small>
                    </div>
                  </CTableDataCell>
                  <CTableDataCell>{getCOITypeBadge(item.coiType)}</CTableDataCell>
                  <CTableDataCell>{item.reason || '-'}</CTableDataCell>
                  <CTableDataCell>{getActionBadge(item.action)}</CTableDataCell>
                  <CTableDataCell>
                    {item.active ? (
                      <CBadge color="success">Active</CBadge>
                    ) : (
                      <CBadge color="secondary">Inactive</CBadge>
                    )}
                  </CTableDataCell>
                  <CTableDataCell>
                    {new Date(item.declaredAt).toLocaleString('vi-VN')}
                  </CTableDataCell>
                </CTableRow>
              ))}
            </CTableBody>
          </CTable>
        )}
      </CCardBody>
    </CCard>
  )
}

export default COIHistory
