import React, { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CRow,
  CCol,
  CTable,
  CTableBody,
  CTableDataCell,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CBadge,
} from '@coreui/react'
import { useTranslation } from 'react-i18next'
import { pcService, COIStatistics as COIStatisticsData, COIType } from '../../services/pc.service'

/**
 * COIStatistics - Trang hiển thị thống kê COI của conference
 *
 * Features:
 * - Overall COI statistics
 * - COI distribution by type
 * - Reviewers with COIs
 * - Recent COIs
 */
const COIStatistics: React.FC = () => {
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const conferenceId = searchParams.get('conferenceId')
    ? parseInt(searchParams.get('conferenceId')!)
    : null
  const [stats, setStats] = useState<COIStatisticsData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (conferenceId) {
      loadStatistics()
    }
  }, [conferenceId])

  const loadStatistics = async () => {
    try {
      setLoading(true)
      const data = await pcService.getCOIStatistics(conferenceId!)
      setStats(data)
    } catch (error: any) {
      console.error('Error loading COI statistics:', error)
      setError(error.response?.data?.message || 'Không thể tải thống kê COI')
    } finally {
      setLoading(false)
    }
  }

  const getCOITypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      CO_AUTHOR: 'Đồng tác giả',
      COLLABORATOR: 'Cộng tác viên',
      ADVISOR: 'Cố vấn',
      INSTITUTIONAL: 'Cùng tổ chức',
      OTHER: 'Khác',
    }
    return labels[type] || type
  }

  const getCOITypeBadge = (type: string) => {
    const colorMap: Record<string, string> = {
      CO_AUTHOR: 'danger',
      COLLABORATOR: 'warning',
      ADVISOR: 'info',
      INSTITUTIONAL: 'secondary',
      OTHER: 'dark',
    }
    return <CBadge color={colorMap[type] || 'secondary'}>{getCOITypeLabel(type)}</CBadge>
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

  if (!stats) {
    return (
      <CCard>
        <CCardBody>
          <CAlert color="info">Không có dữ liệu thống kê</CAlert>
        </CCardBody>
      </CCard>
    )
  }

  return (
    <>
      {/* Overall Statistics */}
      <CRow className="mb-4">
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Tổng số COI</h6>
              <h3>{stats.totalCOIs}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">COI đang active</h6>
              <h3 className="text-success">{stats.activeCOIs}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Reviewers có COI</h6>
              <h3>{stats.reviewersWithCOIs}</h3>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={3}>
          <CCard>
            <CCardBody>
              <h6 className="text-muted">Submissions có COI</h6>
              <h3>{stats.submissionsWithCOIs}</h3>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      {/* COI Distribution by Type */}
      <CCard className="mb-4">
        <CCardHeader>
          <h5>Phân bố COI theo loại</h5>
        </CCardHeader>
        <CCardBody>
          {Object.keys(stats.coiByType).length === 0 ? (
            <p className="text-muted">Chưa có COI nào</p>
          ) : (
            <CTable hover>
              <CTableHead>
                <CTableRow>
                  <CTableHeaderCell>Loại COI</CTableHeaderCell>
                  <CTableHeaderCell>Số lượng</CTableHeaderCell>
                  <CTableHeaderCell>Tỷ lệ</CTableHeaderCell>
                </CTableRow>
              </CTableHead>
              <CTableBody>
                {Object.entries(stats.coiByType)
                  .sort((a, b) => b[1] - a[1])
                  .map(([type, count]) => (
                    <CTableRow key={type}>
                      <CTableDataCell>{getCOITypeBadge(type as COIType)}</CTableDataCell>
                      <CTableDataCell>
                        <strong>{count}</strong>
                      </CTableDataCell>
                      <CTableDataCell>
                        {stats.totalCOIs > 0
                          ? ((count / stats.totalCOIs) * 100).toFixed(1)
                          : 0}
                        %
                      </CTableDataCell>
                    </CTableRow>
                  ))}
              </CTableBody>
            </CTable>
          )}
        </CCardBody>
      </CCard>

      {/* Additional Statistics */}
      <CRow>
        <CCol md={6}>
          <CCard>
            <CCardHeader>
              <h6>Thống kê bổ sung</h6>
            </CCardHeader>
            <CCardBody>
              <div className="mb-3">
                <strong>COI không active: </strong>
                <CBadge color="secondary">{stats.inactiveCOIs}</CBadge>
              </div>
              <div className="mb-3">
                <strong>COI gần đây (30 ngày): </strong>
                <CBadge color="info">{stats.recentCOIs}</CBadge>
              </div>
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={6}>
          <CCard>
            <CCardHeader>
              <h6>Tổng quan</h6>
            </CCardHeader>
            <CCardBody>
              <p>
                Hội nghị <strong>{stats.conferenceName}</strong> có tổng cộng{' '}
                <strong>{stats.totalCOIs}</strong> khai báo COI, trong đó{' '}
                <strong>{stats.activeCOIs}</strong> đang active.
              </p>
              <p>
                Có <strong>{stats.reviewersWithCOIs}</strong> reviewers đã khai báo COI và{' '}
                <strong>{stats.submissionsWithCOIs}</strong> submissions có COI.
              </p>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
    </>
  )
}

export default COIStatistics
